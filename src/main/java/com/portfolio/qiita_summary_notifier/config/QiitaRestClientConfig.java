package com.portfolio.qiita_summary_notifier.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**memo: QiitaApiClientで使うRestClient
 * ここで設定などを行う
 * 今まではApiClientのコンストラクタで生成していたが、テストのしやすさを考えてconfigに隔離した
 * 記述内容は今までと同じ
 */
@Configuration
public class QiitaRestClientConfig {

    /**memo: Qualifier
     * DIコンテナの中から呼ばれるときに、同じ型のオブジェクトがあるとどれを入れたらいいかわからなくなる
     * なので、名前を付けることで対応付けができるようになる... たぶん
     */
    @Bean
    @Qualifier("qiitaRestClient")
    /**memo: 環境変数の取得方法
     * @Value("プロパティ名")でapplication.propertiesの"プロパティ名"の値を取得できる
     * コマンドライン引数やOSの環境変数、独自に作ったファイルなど基本的に何でも値を持ってこれる？
     * 後者は@PropertySourceなどを使うらしい 
     */
    RestClient qiitaRestClient(@Value("${qiita.api.token}") String apiToken) {
        /**memo: HTTPリクエスト
         * RestClientを作る前にリクエストのタイムアウト設定をしている
         * 接続までに5s, 返事を待つのに10s
         */
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));

        /**memo: RestClientの使い方    
         * インスタンス生成時にRestClientを用意する
         * builder()で独自の設定を追加する 
         * このオブジェクトを使ってリクエストを送るとき、必ずヘッダーに引数の文字列を追加して送る
        */
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
    }
}