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

    // 1設定に合わせた内容を通知
    // このメソッドがexecuteで何度も呼ばれるようになる
    public void executeForSetting(NotificationSetting setting) {

        // 最初にキーワードを成型するが、それはqiita側の都合
        // なのでまずは生のタグ文字列だけを渡す
        String includeTags = setting.getSearchKeywords();
        String excludeTags = setting.getExclusionKeywords();
        
        // 2. Qiita APIから記事を取得
        List<Article> articles = articleProvider.getArticles(includeTags, excludeTags);

        articles.stream()   // デバッグコード
                .forEach(s -> System.out.println(s.getTitle()));
        

        // // 3. 通知済みか確認
        // // streamで書けるのかわからなかったので、forループを使った
        // List<Article> newArticles = new ArrayList<Article>() ;
        // for (Article article : articles) {
        //     Integer settingId = setting.getId();
        //     String articleId = article.getId();
        //     if (!notificationLogMapper.existsBySettingIdAndArticleId(settingId, articleId)) {
        //         newArticles.add(article);
        //     }
        // }
        // // 新しい記事が見つからなかったら見つからなかったことを伝えてメソッドを終える
        // if (newArticles.isEmpty()) {
        //     notificationSender.excuteNotification(setting.getWebhookUrl(), "新規記事がありませんでした");
        //     return;
        // }

        // // 4. geminiで要約
        // // 通知するコンテンツを生成している
        // // 構造化出力にすると大きく変わる(別メソッドかも)ので、
        // // これは別ブランチで作り直す
        // String notificationContents = "";
        // for (Article article : newArticles) {
        //     String summary = summarizer.getSummaryOfArticle(article);
        //     NotificationContent content = 
        //         new NotificationContent(article.getTitle(), article.getUrl(), summary);

        //     notificationContents += content;
        // }

        // // 5. Discordへ通知
        // notificationSender.excuteNotification(
        //     setting.getWebhookUrl(), 
        //     notificationContents);

        // // 6. 通知履歴をDBへ保存
        // // settingIdが複数呼ばれるので先に書いたが意味がない？
        // // 通知履歴を残す目的としてはこのタイミングでログに書くべきだが、
        // // nerArticlesを二回回していることと、
        // // 本当に送れたのか確認を取っていない
        // // それなら4の段階でログに入れたほうがいい気もする
        // // 本来は通知できたか確認作業を入れるべき？
        // Integer settingId = setting.getId();
        // for (Article sendedArticle : newArticles) {
        //     notificationLogMapper.insertLog(settingId, sendedArticle.getId());
        // }
        
    }
}

// 今のままだと三回も通知を送るし、要約内容しか届かない
// タイトルとURLを要約内容と組み合わせたオブジェクト、
// 組み合わせるためのメソッドなどが必要
// geminiの返答内容もjson形式にした方が管理しやすい
