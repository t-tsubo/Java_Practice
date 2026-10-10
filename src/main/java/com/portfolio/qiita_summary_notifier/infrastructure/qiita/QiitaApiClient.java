package com.portfolio.qiita_summary_notifier.infrastructure.qiita;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.ArticleProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
public class QiitaApiClient implements ArticleProvider{

    private final RetryTemplate retryTemplate;
    private final RestClient restClient;

    public QiitaApiClient(@Qualifier("qiitaRestClient") RestClient restClient, RetryTemplate retryTemplate) {
        this.restClient = restClient;
        this.retryTemplate = retryTemplate;
    }

    /**memo: 処理のリトライ
     * retryableアノテーションを使ってリトライ処理が可能になる
     * アノテーションはメインクラス、または設定クラスに@Retryableが必要？
     * 調べた情報やAIが出した情報はバージョンが違うせいか結構違う
     * それぞれ引数で拾う/拾わない例外を設定したり、リトライ回数やディレイ、指数バックオフの設定などもできる
     * IDEに従って書いただけなので詳細がわかってない
     */
    @Override 
    public List<Article> fetchArticles(String includeTags, String excludeTags) {
        
        URI uri = createUri(includeTags, excludeTags);      
        List<Article> articleList = new ArrayList<>();
        try {
            /**memo: APIから記事を取得
             * どのリクエストを送るかと、どうやって受け取るかを決めている
             * get:     GETリクエストに設定
             * uri:     uriを設定
             * retrieve:ここで初めてリクエストが送信  例外が発生する可能性がある
             * body:    返ってきたjsonをDTOクラスの配列オブジェクトにする
             *          body()は双方向(Java<=>json)に変換可能
             */
            retryTemplate.invoke(() -> {
                QiitaArticleDto[] dtos = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(QiitaArticleDto[].class);

                // 記事が取得できていればコンバート
                if (dtos != null) {
                    for (QiitaArticleDto dto : dtos) {
                        articleList.add(convertToArticle(dto));
                    }
                }
                log.info("取得した記事数: {}件", articleList.size());   
            });

            return articleList;
            
        } catch(RestClientException e) {
            log.error("QiitaAPIエラー", e);
            return new ArrayList<>();
        } 
    }
    // Queryを作成するメソッド
    private URI createUri(String includeTags, String excludeTags) {

        // "ポエム, , MCP" -> {"ポエム", " ", "MCP"}
        List<String> excludeKeywords = Arrays.asList(excludeTags.split(","));
        /**memo: Queryを作るstreamの流れ
         * streamを通して除外するqueryを作っていく
         * stream() -> 中間処理 -> 終端処理の流れ
         * 必ず終端処理が必要
         * collect -> 変数に代入する(オブジェクトとして残す)とき
         * forEach -> そのまま出力する(plintlnなど)とき
         * # 中間処理
         * map      : trim()で空白を取り除く
         * filter   : !isEmpty()で空文字を取り除く
         * map      : "-tag:"と除外タグをつなげる
         * # 終端処理
         * collect  : Collectors.joining()で区切り文字と組み合わせる
         */
        String excludeQuery = excludeKeywords.stream()
                .map(s -> s.trim())                             
                .filter(s -> !s.isEmpty())                      
                .map(s -> "-tag:" + s)            
                .collect(Collectors.joining(" "));
        // 出力例: "ポエム, , MCP" -> -tag:ポエム -tag:MCP
        /**memo: slf4jの使い方
         * slf4jはログ出力を行うためのライブラリ
         * application.propertiesで設定したログレベルに合わせて、コンソールに出力する
         * 本番環境ではレベルを変更したりしてログを汚さないことができる
         * 使い方:
         * クラスに@Slf4jというアノテーションをつける
         * クラス変数(log)ができるので、そこからメソッドを呼び出す
         * info:    マイルストーン的に処理が起きたら必ず見たいとき
         * debug:   開発中やバグが起こった時に変数の中身などを知りたいとき
         * error:   例外が起きたときにpringlnの代わりに使う
         *          引数の最後に例外オブジェクト(e)をそのまま投げることで詳細が分かる
         * {}はプレースホルダ msgの後に引数を可変で入れられる
         */
        log.debug("excludeQueryの内容: {}", excludeQuery);

        // "Java, spring" -> {"Java, spring"}
        List<String> includeKeywords = Arrays.asList(includeTags.split(","));
        String searchQuery = includeKeywords.stream()
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .map(s -> "tag:" + s + " " + excludeQuery)  // ここで-tagをつなげる
                .collect(Collectors.joining(" OR "));
        // 出力例: "tag:Java -tag:ポエム -tag:MCP OR tag:spring -tag:ポエム -tag:MCP"
        log.debug("searchQueryの内容: {}", searchQuery);

        // uriを安全に作るためのBuilderクラス
        // uriオブジェクト: "https://qiita.com/api/v2/items?query=" + searchQuery + " sort:created&page=1&per_page=3";
        URI uri = UriComponentsBuilder.fromUriString("https://qiita.com/api/v2/items")
                .queryParam("query", searchQuery + " sort:created")
                .queryParam("page", 1)
                .queryParam("per_page", 1)
                .encode()   // uriのエンコードを指定 デフォルトでUTF-8
                .build()    // UriComponentsオブジェクトを生成
                .toUri();   // Uriオブジェクトに変換
        
        return uri;
    }

    // DTOをArticleに変換するメソッド
    private Article convertToArticle(QiitaArticleDto dto) {
        return new Article(
            dto.getTitle(),
            dto.getUrl(),
            dto.getBody(),
            dto.getId(),
            dto.getUser(),
            dto.getTags()
        );
    }
}
