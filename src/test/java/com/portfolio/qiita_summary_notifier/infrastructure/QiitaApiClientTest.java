package com.portfolio.qiita_summary_notifier.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.portfolio.qiita_summary_notifier.config.RetryConfig;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.QiitaApiClient;

class QiitaApiClientTest {

    /**memo: テストの流れ
     * 結構長くなりそうなので大雑把に把握するところまでにとどめる
     * 1. RestClientのbuilder()を使ったところで止める(イメージ)
     * 2. RestClientに設定を追加するためにserverクラスを紐づける
     * 3. serverが返してほしい応答とオブジェクトを定義する
     * 4. 今まで作った設定をまとめて、RestClientとしてbuildする
     * 5~ 実際のテスト内容
     */
    @Test
    void HTTP応答が200なら記事を取得できる() {
        // 1. テスト用のRestClient.Builderを作る
        RestClient.Builder builder = RestClient.builder();

        // 2. BuilderにMockRestServiceServerを結び付ける
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        // 3. 「このリクエストが来たら、200とこのJSONを返す」と設定する
        server.expect(request -> {
            assertEquals("qiita.com", request.getURI().getHost());
        }).andRespond(withSuccess("""
                [{
                  "title": "テスト記事",
                  "url": "https://qiita.com/example",
                  "body": "本文",
                  "id": "article-1",
                  "user": {"id": "user-1", "name": "テストユーザー"},
                  "tags": [{"name": "Java"}]
                }]
                """, MediaType.APPLICATION_JSON));

        // 4. 設定済みBuilderからRestClientを作る
        RestClient restClient = builder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .build();

        // 5. テスト用RestClientとRetryTemplateを渡して、対象クラスを作る
        RetryTemplate retryTemplate = new RetryConfig().qiitaRetryTemplate();
        QiitaApiClient apiClient =
                new QiitaApiClient(restClient, retryTemplate);

        // 6. 本物のfetchArticles()を呼ぶ
        var articles = apiClient.fetchArticles("Java", "");

        // 7. 戻り値を確認する
        assertEquals(1, articles.size());
        assertEquals("テスト記事", articles.get(0).getTitle());

        // 設定したHTTP応答が実際に使われたことを確認する
        server.verify();
    }

    @Test
    void HTTP応答が400ならリトライせず空のリストを返す() {
        TestFixture fixture = createTestFixture();

        // 400 Bad Requestを返す
        fixture.server.expect(once(), request -> {
            assertEquals("qiita.com", request.getURI().getHost());
        }).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        var articles = fixture.apiClient.fetchArticles("Java", "");

        // QiitaApiClientが例外を捕捉し、空のリストを返すことを確認
        assertTrue(articles.isEmpty());

        // リクエストが1回だけだったことも確認
        fixture.server.verify();
    }

    @Test
    void HTTP応答が500なら再試行後に空のリストを返す() {
        TestFixture fixture = createTestFixture();

        // maxRetries(3)なので、初回1回＋再試行3回＝合計4回
        fixture.server.expect(times(4), request -> {
            assertEquals("qiita.com", request.getURI().getHost());
        }).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        var articles = fixture.apiClient.fetchArticles("Java", "");

        // 規定回数の再試行後も失敗したので、空のリストを返す
        assertTrue(articles.isEmpty());

        // 4回リクエストされたことを確認
        fixture.server.verify();
    }

    private TestFixture createTestFixture() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .build();

        RetryTemplate retryTemplate = new RetryTemplate(
                RetryPolicy.builder()
                        .maxRetries(3)
                        .includes(
                                ResourceAccessException.class,
                                HttpServerErrorException.class)
                        .excludes(HttpClientErrorException.class)
                        .delay(Duration.ZERO)
                        .build());

        QiitaApiClient apiClient =
                new QiitaApiClient(restClient, retryTemplate);

        return new TestFixture(apiClient, server);
    }

    private record TestFixture(
            QiitaApiClient apiClient,
            MockRestServiceServer server) {
    }
}

/**memo: springを起動しない単体テスト
 * テストしたいコードのロジックを見たいときは単体テストをする
 * 単体テストはspringを起動せずに行うのが基本
 * 単体テストであればほかのコード部分がコンパイルできない状態でもテストが可能(テストするコードに関係あるところしか見ない)
 * springを起動しないので、DIなどは使えない
 * そういう時は自分でオブジェクトを作る必要がある
 */