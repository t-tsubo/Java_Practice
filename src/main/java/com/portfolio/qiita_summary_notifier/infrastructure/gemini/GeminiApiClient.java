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
import com.portfolio.qiita_summary_notifier.utility.Utility;

@Component
public class GeminiApiClient implements Summarizer{
    
    private final RestClient restClient;
    // ハードコーディング 
    // プロンプトの内容や出力形式は今後ブラッシュアップする
    // 現時点ではMVPとして動けばOK
    // Structured Outputというllm関連の用語があるのでそちらも参考(構造化出力、jsonで吐き出してくれる)
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

    // コンストラクタでヘッダーの設定(Qiitaの奴と同じ)
    public GeminiApiClient(@Value("${gemini.api.token}") String apiToken) {
        /*
        HttpClientやJdkClientでのタイムアウトはHTTPリクエストごとに必要になる
        現時点ではすべてのAPIで書いているが、似たようなコードが複数あるので良い形ではない
        @Configurationとconfigパッケージなどを作り、そこで管理することになる
        */

        // タイムアウトを設定するために標準HttpClientを作成
        // コネクションタイムアウトは先に作る必要があるらしい
        HttpClient httpClient = HttpClient.newBuilder()
                // Durationクラスは時間量を表現するためのクラス
                // 10秒たつとタイムアウトする
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // タイムアウトを設定したHttpClientを引数に渡し、オブジェクトを生成
        // このオブジェクトをRestClientに渡すことで設定をセットする
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        // 返事が60秒返ってこなければタイムアウト
        factory.setReadTimeout(Duration.ofSeconds(60));

        restClient = RestClient.builder()
                // 作った設定(factoryオブジェクト)をセットする
                .requestFactory(factory)
                // [1.サーバ] + [2.APIのバージョン] + [3.機能] がREST APIの基本形 
                // baseUrlにはこの1と2を指定しておき、3をuriで指定する
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                // ヘッダーに:(コロン)はいらない 自動で入力される
                .defaultHeader("x-goog-api-key", apiToken)
                // これはjson形式で送りますということを表している
                // Jacksonが自動で書いてくれるけど、今回は明示的に書いた
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String getSummaryOfArticle(Article article) {
        GeminiRequestDto request = new GeminiRequestDto(
                this.prompt + article.getBody());

        try {
            GeminiResponseDto response = restClient.post()    
                    .uri("/interactions")
                    .body(request)
                    .retrieve()
                    .body(GeminiResponseDto.class);
            
            return response.getSummaryText();

        } catch (RestClientException e) {
            Utility.writeUtf8Text("logs/summaryLog.txt", "Gemini APIエラー: " + e.getMessage());
            return "要約失敗";
        }
    }

    // 生Jsonを見るためのメソッド デバッグ用
    // public String test(Article article) {
    //     GeminiRequestDto request = new GeminiRequestDto(article.getBody());
    //     try {
    //         String rawJson = restClient.post()
    //                 .uri("/interactions")
    //                 .body(request) 
    //                 .retrieve()
    //                 .body(String.class);
            
    //         System.out.println(rawJson);
    //         return rawJson;

    //     } catch (RestClientException e) {
    //         System.out.println("Gemini APIエラー" + e.getMessage());
    //         return "要約失敗";
    //     }
    // }
}
