package com.portfolio.qiita_summary_notifier.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto.Tag;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto.User;
import com.portfolio.qiita_summary_notifier.repository.NotificationLogMapper;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;

class QiitaNotificationServiceTest {

    /**memo: モックを使ったテストの方法
     * 大まかな流れ:
     * 1~3はモック作成やルールの定義
     *  ここでは処理は走らず、どういった動きをしてほしいのかを記述する
     * 4で実際にテストが実行される
     *  1~3で作ったモックや定義した処理を実行する
     *  このときモックはログをメモリに記憶し続ける
     * 5~6で4のログを見ながら検証する
     *  メソッドの呼ばれた回数や、期待する出力となっているかを確認する
     *  
     * 1:   部品をモックとして生成する
     *      引数に与えたクラスとして動く
     * 2:   テスト用のデータを作る
     *      ここは手入力でテスト用のデータを入れていく
     * 3:   モックが呼ばれた時の動作を決める
     *      when(メソッド).thenReturn(戻り値)
     *      whenの引数にあるメソッドの処理をすり替えて、thenReturnの引数の値を返す
     * 4:   テストの実行
     *      ここでテストしたいメインのメソッドを呼ぶ(今回は一連の流れを実行するdeliverArticlesメソッドを呼ぶ)
     *      このとき、このメソッドの過程で行われるfetchArticlesなどのメソッドは3で定義したmockにすり替わっている
     * 5:   期待した処理が呼ばれたかを確認する
     *  verify(モック, [times(回数)]).メソッド()
     *      引数のモックがメソッドを呼び出したのかを確認する
     *      回数は何度呼び出されてほしいかを入れる(省略可能)
     *  ArgumentCaptor.forClass(クラス名.class)
     *      caputureしたいオブジェクトのクラス名を記述
     *  verify().メソッド(argument.capture())
     *      メソッドの引数に与えた値を取り出す
     *      テストしたときのログから実際に与えた引数のオブジェクトを取得できる
     *      引数が複雑なオブジェクトだったりして、自分で用意するのが大変な時などに使う
     *  argument.getValue()
     *      キャプチャしたオブジェクト(値)を取り出すためのメソッド
     *      argumentは取り出した値に置き換わるわけでないので、このメソッドが必要
     */

    @Test
    void deliverArticles_未通知の記事を要約して通知し履歴を保存する() {
        // 1. サービスが依存する部品をモックにする
        ArticleProvider articleProvider = mock(ArticleProvider.class);
        NotificationSender notificationSender = mock(NotificationSender.class);
        Summarizer summarizer = mock(Summarizer.class);
        NotificationSettingMapper notificationSettingMapper =
                mock(NotificationSettingMapper.class);
        NotificationLogMapper notificationLogMapper =
                mock(NotificationLogMapper.class);

        // モックを渡して、テスト対象のサービスを作る
        QiitaNotificationService service = new QiitaNotificationService(
                articleProvider,
                notificationSender,
                summarizer,
                notificationSettingMapper,
                notificationLogMapper
        );

        // 2. テスト用の入力を用意する
        NotificationSetting setting = new NotificationSetting();
        setting.setId(10);
        setting.setIncludeTags("Java");
        setting.setExcludeTags("ポエム");
        setting.setWebhookUrl("https://example.com/webhook");

        User user = new User();
        user.setId("001");
        user.setName("テストユーザー");

        Tag tag = new Tag();
        tag.setName("Java");

        Article article = new Article();
        article.setId("article-001");
        article.setTitle("テスト記事");
        article.setUrl("https://qiita.com/example/items/article-001");
        article.setUser(user);
        article.setTags(List.of(tag));

        GeminiResponseDto summary = new GeminiResponseDto();
        summary.setTheme("Javaのテスト");
        summary.setKeyTakeaways("モックを使うと外部通信なしでテストできる");
        summary.setTargetAudience("Java初学者");

        // 3. モックの動作を決める
        // Qiitaからこの記事が取得されたことにする
        when(articleProvider.fetchArticles("Java", "ポエム"))
                .thenReturn(List.of(article));

        // 通知履歴にはまだ存在しない記事とする
        when(notificationLogMapper.existsBySettingIdAndArticleId(10, "article-001"))
                .thenReturn(false);

        // Geminiから要約が返ってきたことにする
        when(summarizer.fetchSummaryOfArticle(article))
                .thenReturn(summary);

        // 4. テスト対象の処理を実行する
        service.deliverArticles(setting);

        // 5. 期待した処理が呼ばれたか確認する
        verify(articleProvider, times(1)).fetchArticles("Java", "ポエム");
        verify(summarizer).fetchSummaryOfArticle(article);

        // 通知時に渡された記事の内容を取り出す
        ArgumentCaptor<List> articlesCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationSender).notifyNewArticles(
                eq("https://example.com/webhook"),  // captureを使う場合、ほかの引数もepなどのメソッドが必要になる？
                articlesCaptor.capture()
        );

        List<?> sentArticles = articlesCaptor.getValue();
        assertEquals(1, sentArticles.size());

        // sentArticlesはリストなので、get(0)で取り出す
        // ジェネリクスで拾ってきたのでキャストしている
        ArticleWithSummary sentArticle =
                (ArticleWithSummary) sentArticles.get(0);
        assertEquals("テスト記事", sentArticle.getTitle());
        assertEquals("Javaのテスト", sentArticle.getTheme());
        assertEquals("モックを使うと外部通信なしでテストできる",
                sentArticle.getKeyTakeaways());
        assertEquals("https://qiita.com/example/items/article-001",
                sentArticle.getUrl());

        // 通知した記事の履歴が保存されたことを確認する
        verify(notificationLogMapper).insertLog(10, "article-001");
    }
}