package com.portfolio.qiita_summary_notifier.infrastructure.qiita;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.ArticleProvider;
import com.portfolio.qiita_summary_notifier.utility.Utility;

// 現時点では役割が決まっていない(？)ので
// ComponentとしてDIに登録する
@Component 
public class QiitaApiClient implements ArticleProvider{

    private final RestClient restClient;

    // @Value("プロパティ名")でapplication.propertiesの
    // "プロパティ名"の値を取得できる
    // コマンドライン引数やOSの環境変数、
    // 独自に作ったファイルなど、基本的に何でも値を持ってこれる？
    // 後者は@PropertySourceなどを使うらしい
    public QiitaApiClient(@Value("${qiita.api.token}") String apiToken) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60));

        // インスタンス生成時にRestClientを用意する
        // builder()で独自の設定を追加する
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                // このオブジェクトを使ってリクエストを送るとき、
                // 必ずヘッダーに引数の文字列を追加して送る
                // 生成されるヘッダー：Authorization Bearer トークン
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
    }

    @Override 
    public List<Article> getArticles(String tag) {
        // APIをたたく時の文字列(メッセージ)
        // 今回はtagにJava、1ページに3記事を取得するという内容
        String url = "https://qiita.com/api/v2/items?query=tag:" + tag + "&page=1&per_page=3";
        // RestClientは例外を発生させる可能性があるのでtryで囲む       
        try {
            // APIをたたき、QiitaArticleDto型の配列に格納する
            // HTTPのGETリクエストを生成している(まだ送っていない)
            QiitaArticleDto[] dtos = restClient.get()
            // リクエストの送り先を指定
                    .uri(url)
            // このメソッドで初めてリクエストが送られる
            // ここで初めて例外が発生する可能性が出てくる
                    .retrieve()
            // レスポンスの内容(jsonテキスト)を引数のクラスの型のオブジェクトにする
            // body()自体は双方向の変換が可能 JSON ⇔ Java
                    .body(QiitaArticleDto[].class);

            // 記事の配列を受け取る空のlist
            List<Article> articleList = new ArrayList<>();
            // 取得できたら配列から一つずつ取り出して、
            // コンバートして格納
            if (dtos != null) {
                for (QiitaArticleDto dto : dtos) {
                    articleList.add(convertToArticle(dto));
                }
            }

            return articleList;

        } catch(RestClientException e) {
            Utility.writeUtf8Text("notes/debug_log/qiitaArticlesLog.txt", "Qiita APIエラー" + e.getMessage());
            return new ArrayList<>();
        }
        
    }

    private Article convertToArticle(QiitaArticleDto dto) {
        return new Article(
            dto.getTitle(),
            dto.getUrl(),
            dto.getBody()
        );
    }
}
