package com.portfolio.qiita_summary_notifier.service;

import java.util.ArrayList;
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
    
    /**fix: バッチ処理の追加とテスト
     * バッチ処理のための@Scheduleなどを調べる
     * テスト実行にはDBに有効なデータを入れる必要がある
     * mockなどを作ってテストしていく必要がある
     */
    // 有効な設定を全件取得してループで回す
    public void execute() {
        List<NotificationSetting> settingsList = 
            notificationSettingMapper.selectActiveSettings();
        for (NotificationSetting setting : settingsList) {
            deliverArticles(setting);
        }
    }

    // 1つの設定に対しての記事取得から通知までの処理
    public void deliverArticles(NotificationSetting setting) {

        // 設定のタグリストを使って最新記事を取得
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
        // まだ送信していない記事のみに振り分ける
        Integer settingId = setting.getId();
        List<Article> latestArticles = articles.stream()
                .filter(article -> !notificationLogMapper.existsBySettingIdAndArticleId(settingId, article.getId()))
                .toList();

        // 新しい記事が見つからなかったら見つからなかったことを伝えてメソッドを終える
        // 例外処理の追加のタイミングで修正
        // 見つからなかったことを伝える専用のembedを作って配信が一番まるそう
        // discordのエラー以外必ず通知したいからフラグ管理にしてgeminiへの問い合わせをif文で囲った方がよさそう？
        // if (latestArticles.isEmpty()) {
        //     notificationSender.notifyNewArticles(setting.getWebhookUrl(), "新規記事がありませんでした");
        //     return;
        // }

        // 要約を取り出して、記事データと合わせてオブジェクトにする
        // 要約は都度呼び出すことで、例外発生時にスキップできるようになった
        List<ArticleWithSummary> unformattedList = new ArrayList<>();
        for (Article article : latestArticles) {
            ArticleWithSummary unformattedData = 
                new ArticleWithSummary(article, summarizer.fetchSummaryOfArticle(article));
            unformattedList.add(unformattedData);
        }

        // Discordへ通知
        notificationSender.notifyNewArticles(
            setting.getWebhookUrl(), unformattedList
        );

        // 通知履歴をDBへ保存
        for (Article sentArticle : latestArticles) {
            notificationLogMapper.insertLog(settingId, sentArticle.getId());
        }
    }
}
