package com.portfolio.qiita_summary_notifier.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private final NotificationLogMapper NotificationLogMapper;

    // executeForSettingを複数回(DBの設定数ごとに)実行する
    // 最終的にこのメソッドは引数を持たず、@Scheduleによって
    // 一定時間ごとに実行されるだけになる...はず
    // executeでselectActiveSettings()で有効な設定一覧を取得
    // 取得したリストからNotificationSettingオブジェクトを取り出し、
    // 一件ごとのexecuteForSettingメソッドに引数として渡す
    // executeForSettingで引数のオブジェクトから必要な情報を取得して
    // 通知までの一連の流れを行う
    // 多分これでいいからこのメソッド二つの形は間違ってないはず
    public void execute(String query, String webhookUrl) {
        
        // selectActiveSettings()で有効な設定一覧を呼び出す
        // 一覧をループで取り出し、executeForSettingに渡す
        
        List<Article> articles = articleProvider.getArticles(query);
        String nContents = "";
        
        for (Article article : articles) {
            String summary = summarizer.getSummaryOfArticle(article);

            NotificationContent notificationContent = 
                    new NotificationContent(article.getTitle(), article.getUrl(), summary);

            nContents += notificationContent.toDiscordMessage();
        }

        notificationSender.excuteNotification(webhookUrl, nContents);
    }

    // 1設定に合わせた内容を通知
    // このメソッドがexecuteで何度も呼ばれるようになる
    private void executeForSetting(NotificationSetting setting) {
        // 1. DB設定からキーワードを取得し整形
        // DBから"Java,spring"というタグを取得し、カンマで区切る({"Java", "spring"})
        // カンマ区切りの配列をListに変換(asList)
        List<String> searchKeyWords = Arrays.asList(setting.getSearchKeywords().split(","));
        // streamで処理を加えていく
        // 最終的には"tag:Java OR tag:spring"となる
        String apiQuery = searchKeyWords.stream()
                // 空白を取り除く(空白があるタグはほぼない)
                .map(s -> s.trim())
                // カンマの位置がずれていて空文字がある可能性があるのでここで取り除く
                .filter(s -> !s.isEmpty())
                // QiitaAPIにtagで検索するときの文字列
                .map(s -> "tag:" + s)
                // 終端処理 
                // collectはstreamの最後に来る ここまでくるとstreamが流れ始める
                // 引数内(Collectors)では、" OR "を間に入れて連結している
                // streamをつないで、最終的に変数に格納したいときはcollect
                // 変数に保存せず、そのまま出力する(println()など)ならforEach
                .collect(Collectors.joining(" OR "));

        // 2. Qiita APIから記事を取得
        List<Article> articles = articleProvider.getArticles(apiQuery);
        // 3. 通知済みか確認

        // 4. 要約

        // 5. Discordへ通知

        // 6. 通知履歴をDBへ保存
    }
}

// 今のままだと三回も通知を送るし、要約内容しか届かない
// タイトルとURLを要約内容と組み合わせたオブジェクト、
// 組み合わせるためのメソッドなどが必要
// geminiの返答内容もjson形式にした方が管理しやすい
