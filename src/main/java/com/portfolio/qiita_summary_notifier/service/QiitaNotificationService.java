package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.repository.NotificationLogMapper;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;

import lombok.RequiredArgsConstructor;

// このクラスでインフラの3つのメソッドを使って出力する
// ここで出力内容も整理すればいい...はず

@Service
@RequiredArgsConstructor
public class QiitaNotificationService {

    private final ArticleProvider articleProvider;
    private final NotificationSender notificationSender;
    private final Summarizer summarizer;

    private final NotificationSettingMapper notificationSettingMapper;
    private final NotificationLogMapper notificationLogMapper;

    // executeForSettingを複数回(DBの設定数ごとに)実行する
    // 最終的にこのメソッドは引数を持たず、@Scheduleによって
    // 一定時間ごとに実行されるだけになる...はず
    // executeでselectActiveSettings()で有効な設定一覧を取得
    // 取得したリストからNotificationSettingオブジェクトを取り出し、
    // 一件ごとのexecuteForSettingメソッドに引数として渡す
    // executeForSettingで引数のオブジェクトから必要な情報を取得して
    // 通知までの一連の流れを行う
    // 多分これでいいからこのメソッド二つの形は間違ってないはず
    // public void execute(String query, String webhookUrl) {
        
    //     // selectActiveSettings()で有効な設定一覧を呼び出す
    //     // 一覧をループで取り出し、executeForSettingに渡す
        
    //     List<Article> articles = articleProvider.getArticles(query);
    //     String nContents = "";
        
    //     for (Article article : articles) {
    //         String summary = summarizer.getSummaryOfArticle(article);

    //         NotificationContent notificationContent = 
    //                 new NotificationContent(article.getTitle(), article.getUrl(), summary);

    //         nContents += notificationContent.toDiscordMessage();
    //     }

    //     notificationSender.excuteNotification(webhookUrl, nContents);
    // }

    // このメソッドがexecuteで何度も呼ばれるようになる
    public void deliverArticles(NotificationSetting setting) {

        // 最初にキーワードを成型するが、それはqiita側の都合
        // なのでまずは生のタグ文字列だけを渡す
        String includeTags = setting.getIncludeTags();
        String excludeTags = setting.getExcludeTags();

        List<Article> articles = articleProvider.fetchArticles(includeTags, excludeTags);
        
        /**メモ: stream関連
         * このメソッドは取得した記事リストから、通知していない新着記事のみにしたい
         * 取得したarticlesをstreamで流していく
         * settingのidは共通なので、このタイミングで取得しておく
         * filter:  articleを受け取って記事が通知済みでないかを確認している
         *          exists...()メソッドの引数にarticle.getId()をすることで記事のidを取得して渡している
         * toList:  ArrayListに変換するメソッド  Java16から実装された
         *          イミュータブルなオブジェクトなので注意
         *          collect(Collectors.toList())であればミュータブルなリストとして扱える
         */
        Integer settingId = setting.getId();
        List<Article> latestArticles = articles.stream()
                .filter(article -> !notificationLogMapper.existsBySettingIdAndArticleId(settingId, article.getId()))
                .toList();

        // 新しい記事が見つからなかったら見つからなかったことを伝えてメソッドを終える
        // 作り直しが必要
        // if (latestArticles.isEmpty()) {
        //     notificationSender.notifyNewArticles(setting.getWebhookUrl(), "新規記事がありませんでした");
        //     return;
        // }

        /**メモ: geminiの構造化出力実装のタイミングで更新が必要
         * geminiAPIの取得する内容によって変わる
         * streamで実装したい
         * 
         * 更新中
         * latestarticlesとfetchSummaryOfArticleから作ったDtoリストを使って
         * ArticleWithSumarryのリストを作る必要がある
         * pythonのzipして回すイメージ
         * IntStreamというクラスをAPIを使うか、googleのguavaというライブラリを使えばいいっぽい？
         */
        // List<ArticleWithSummary> articleWithSummaries = latestArticles.stream()
        //         .map(article -> summarizer.fetchSummaryOfArticle(article))
        //         .map(article -> new ArticleWithSummary(article, ))
        //         .

        // // Discordへ通知
        // notificationSender.notifyNewArticles(
        //     setting.getWebhookUrl(), notificationContents
        // );

        // 通知履歴をDBへ保存
        for (Article sentArticle : latestArticles) {
            notificationLogMapper.insertLog(settingId, sentArticle.getId());
        }
    }
}
