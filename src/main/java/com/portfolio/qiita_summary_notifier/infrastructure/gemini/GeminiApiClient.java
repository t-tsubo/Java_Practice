package com.portfolio.qiita_summary_notifier.infrastructure.gemini;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.Content;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.CreateModelInteractionResponseFormat;
import com.google.genai.gaos.models.interactions.Interaction;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.Model;
import com.google.genai.gaos.models.interactions.ModelOutputStep;
import com.google.genai.gaos.models.interactions.ResponseFormat;
import com.google.genai.gaos.models.interactions.Step;
import com.google.genai.gaos.models.interactions.TextContent;
import com.google.genai.gaos.models.interactions.TextResponseFormat;
import com.google.genai.gaos.models.interactions.TextResponseFormatMimeType;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import com.google.genai.types.ClientOptions;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.Summarizer;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import tools.jackson.databind.ObjectMapper;

@Slf4j 
@Component
public class GeminiApiClient implements Summarizer{
    
    private final Client client;
    private final ObjectMapper objectMapper;

    public GeminiApiClient(@Value("${gemini.api.token}") String apiToken, ObjectMapper objectMapper) {

        // Json->ResponseDtoへの変換をするオブジェクト
        this.objectMapper = objectMapper;

        /**memo: geminiAPIのClientオブジェクトを作成
         * OkHttpClientでタイムアウト設定をしている
         * geminiAPIライブラリは内部でOkHttpClientという別のライブラリを採用している(?)のでこちらの記述方法
         * callTimeout: 全体のタイムアウト時間
         * 
         * ClientはgeminiAPIライブラリのオブジェクト
         * 構造化スキーマやプロンプトの設定からHTTPリクエストまで全部やるオブジェクト
         * new Client:      環境変数にapiKeyを置いておけば自動で読み込むので.apiKey()が必要ない
         *                  今回はlocalから読み込むので.apiKeyが必要だった
         * builder, build:  ビルダーパターンのひとつ
         *                  最近のライブラリだとよく使われるらしい(RestClientもそうだった)
         * apiKey:          apiKeyを設定  
         * clientOptions:   引数でOkHttpClientオブジェクトをhttpClientオブジェクトとして作り直している(?)
         */
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10)) // 接続確立まで
                .readTimeout(Duration.ofSeconds(60))    // データ受信中の待ち時間
                .callTimeout(Duration.ofSeconds(90))    // リクエスト全体
                .build();
        client = Client.builder()
                .apiKey(apiToken)
                .clientOptions(ClientOptions.builder()
                        .customHttpClient(httpClient)
                        .build())   
                .build();

    }

    /**memo: ここにgeminiAPIライブラリを追加
     * 構造化のスキーマをprivateメソッドで追加
     * tryブロックのrestClientで送っている部分をすべてgeminiAPIライブラリの
     * clientオブジェクトに任せる
     */
    @Override
    public GeminiResponseDto fetchSummaryOfArticle(Article article) {

        // スキーマの定義からプロンプトまで作成
        CreateModelInteraction params = createPrompt(article.getTitle(), article.getBody());
        // ここでリクエストを送って戻ってきたものをinteraction変数に格納
        Interaction interaction = 
            client.interactions.create(CreateInteractionRequestBody.of(params)).interaction().get();

        log.debug("interactionの中身: {}", interaction);
        
        // 要約を取り出す処理
        StringBuilder output = new StringBuilder();
        for (Step step : interaction.steps().orElse(List.of())) {
            if (step instanceof ModelOutputStep modelOutputStep) {
                for (Content content : modelOutputStep.content().orElse(List.of())) {
                    if (content instanceof TextContent textContent) {
                        textContent.text().ifPresent(output::append);
                    }
                }
            }
        }

        try {
            return objectMapper.readValue(output.toString(), GeminiResponseDto.class);
        } catch(Exception e) {
            /**fix: 例外処理
             * どう処理をするか考えないといけない
             */
            log.error("geminiAPIエラー: {}", e);
            return new GeminiResponseDto();
        }
        // SummaryCache.saveCacheOfSummaryJson(article.getId(), output.toString());
        // return output.length() > 0 ? output.toString() : "geminiAPIエラー";
    }

    private CreateModelInteraction createPrompt(String title, String body) {

        // themeプロパティの定義
        Map<String, Object> themeProp = new HashMap<>();
        themeProp.put("type", "string");
        themeProp.put("description", "記事のテーマと使われている技術を一文で記述してください。");
        // key_takeawaysプロパティの定義
        Map<String, Object> keyTakeawaysProp = new HashMap<>();
        keyTakeawaysProp.put("type", "string");
        keyTakeawaysProp.put("description", "記事の魅力と読むことで得られるメリットを一文で記述してください。");
        // target_audiencePropプロパティの定義
        Map<String, Object> targetAudienceProp = new HashMap<>();
        targetAudienceProp.put("type", "string");
        targetAudienceProp.put("description", "記事が想定している読者層を一文で記述してください。");
        // propertiesプロパティの定義
        Map<String, Object> propertiesProp = new HashMap<>();
        propertiesProp.put("theme", themeProp);
        propertiesProp.put("key_takeaways", keyTakeawaysProp);
        propertiesProp.put("target_audienceProp", targetAudienceProp);
        // 最終的にgeminiへ渡す全体の設計図
        Map<String, Object> summaryJsonSchema = new HashMap<>();
        summaryJsonSchema.put("type", "object");
        summaryJsonSchema.put("properties", propertiesProp);
        summaryJsonSchema.put("required", Arrays.asList("theme", "key_takeaways", "target_audienceProp"));

        // ここにプロンプトとtitle, bodyを与えて要約してもらう
        String prompt = """
                あなたは優秀な技術記事のキュレーターです。
                与えられた記事の内容を、Discordで共有するための「3行サマリー」に要約してください。
                出力は必ず以下の3行のみとし、それぞれの行は指定された役割を厳守してください。

                【出力フォーマットと各行の役割】
                1行目（テーマと技術）: 記事の主題と、扱っている具体的な技術名やツール名を明記する。
                2行目（記事の魅力）: 読者がこの記事を読むことで得られる具体的なメリットや、実践的な学びの内容を記載する。
                3行目（対象読者）: 「〇〇に悩んでいる人」「〇〇をこれから始める開発者」など、抱えている課題や具体的な状況でターゲットを表現する。

                【出力例】
                1行目: ReactとTypeScriptを用いた、コンポーネント設計と状態管理のベストプラクティスについての記事です。
                2行目: 再利用性の高いコンポーネントの分割手法や、パフォーマンス低下を防ぐ具体的な実装パターンをコード付きで学べます。
                3行目: プロジェクトの規模が大きくなり、Propsのバケツリレーやレンダリングの最適化に課題を感じているフロントエンドエンジニア向け。

                【タイトル】
                %s
                【本文】
                %s
                """.formatted(title, body);

        CreateModelInteractionResponseFormat format = 
            CreateModelInteractionResponseFormat.of(
                ResponseFormat.of(
                    TextResponseFormat.builder()
                            .mimeType(TextResponseFormatMimeType.APPLICATION_JSON)
                            .schema(summaryJsonSchema)
                            .build()
                )
            );
        
        CreateModelInteraction params = 
            CreateModelInteraction.builder()
                    .model(Model.of("gemini-3.5-flash-lite"))
                    .input(InteractionsInput.of(prompt))
                    .responseFormat(format)
                    .build();

        return params;

    }

}
