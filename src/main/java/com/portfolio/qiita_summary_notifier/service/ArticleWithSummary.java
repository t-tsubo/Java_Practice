package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;

import lombok.Data;

/**memo: DiscordWebhookClientで配信するコンテンツを作るために必要な情報を持つDTOクラス
 * titleやsummaryの中身、urlなどを保持する
 */
@Data
public class ArticleWithSummary {
    private String title;
    private List<String> tags;
    private String username;
    private String userId;
    private String theme;
    private String keyTakeaways;
    private String targetAudience;
    private String url;

    /**memo: 三項演算子などを使ったデータ取り出し
     * Discordに渡す時点でほかのクラスに依存しないデータ型に直しておく
     * tags:    この時点でList<String>に変換する
     *          streamでgetName()を読んでリストにする
     * user:    ユーザー名はnullの可能性がある
     *          nullじゃない場合、userの名前をそのまま使う
     *          nullの場合、userのidを使う
     */
    public ArticleWithSummary(Article article,  GeminiResponseDto summary) {
        this.title = article.getTitle();
        this.tags = article.getTags().stream().map(tag -> tag.getName()).toList();
        this.username = article.getUser().getName();
        this.userId = article.getUser().getId();
        this.theme = summary.getTheme();
        this.keyTakeaways = summary.getKeyTakeaways();
        this.targetAudience = summary.getTargetAudience();
        this.url = article.getUrl();

        
    }
}
