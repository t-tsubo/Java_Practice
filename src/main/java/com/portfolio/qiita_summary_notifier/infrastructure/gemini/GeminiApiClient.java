package com.portfolio.qiita_summary_notifier.infrastructure.gemini;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiRequestDto;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.Summarizer;

@Component
public class GeminiApiClient implements Summarizer{
    
    private final RestClient restClient;
    /**メモ: プロンプト(要修正)
     * プロンプトを構造化出力に切り替える
     * Structured Outputというllm関連の用語があるのでそちらも参考
     */
    private final String prompt = """
            以下はQiitaの記事です。
            まだこの記事を読んでいない読者が読んでみたくなる要約をしてください。
            # 出力条件
            - 本文内容を箇条書きで3行にまとめる。
            - ですます調を使う。
            - 本文を読むうえで、初心者や経験者、専門的など、どれくらいの知識レベルが必要かを明示する。
                - 本文要約の3行に追加して情報を付加する。
            # 以下本文
            """;

    public GeminiApiClient(@Value("${gemini.api.token}") String apiToken) {
        /**メモ: タイムアウト設定
         * HttpClientやJdkClientでのタイムアウトはHTTPリクエストごとに必要になる
         * 現時点ではすべてのAPIで書いているが、似たようなコードが複数あるので良い形ではない
         * @Configurationとconfigパッケージなどを作り、そこで管理することになる
        */
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60));

        /**メモ: RestClient設定
         * ベースのURLとヘッダーを設定している
         * requestFactory:  factoryオブジェクト(タイムアウト設定)を渡す
         * baseUrl:         [1.サーバ] + [2.APIのバージョン] + [3.機能] がRestAPIの基本形
         * defaultHeader:   apikeyを渡す(コロンは自動入力)
         * defaultHeader:   json形式で送ることを明示(デフォルトでjson)
         */
        restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("x-goog-api-key", apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String fetchSummaryOfArticle(Article article) {
        GeminiRequestDto request = new GeminiRequestDto(
                this.prompt + article.getBody());

        try {
            GeminiResponseDto response = restClient.post()    
                    .uri("/interactions")  // これだけなのでURIBuilderは使わない
                    .body(request)
                    .retrieve()
                    .body(GeminiResponseDto.class);
                    // .body(String.class); // デバッグ用 生json
            // GeminiのRPM15制限に引っ掛からないためのディレイ
            Thread.sleep(Duration.ofSeconds(5));
        
            return response.getSummaryText();

        } catch (RestClientException e) {
            // 一記事が要約できなくても他が成功する可能性があるので処理を止めない
            System.out.println("GeminiAPIエラー: " + e.getMessage());
            return "要約失敗";
        } catch (InterruptedException e) {
            /**メモ: InterruptedException
             * InterruptedExceptionはサーバー側で再起動したり、割り込み操作が行われたときだけ起こる
             * 起こる頻度が少ないことと、起きるときはほかの処理も続けられないので
             * 例外を投げてこの処理を中断する
             * currentThread().interrupt(): 割り込みしてスレッドをストップさせている
             * 元の例外は履歴を残さない？ので同じ動作を再現している
             */
            Thread.currentThread().interrupt();
            throw new RuntimeException("API待機中にエラーが発生しました", e);
        }
    }
}
