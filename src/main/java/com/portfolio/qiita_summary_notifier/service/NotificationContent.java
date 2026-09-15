package com.portfolio.qiita_summary_notifier.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor 
public class NotificationContent {
    
    private String title;
    private String url;
    private String summary;

    public String toDiscordMessage() {
        // テキストブロックで整形して返す
        // テキストブロックに変数を埋め込むにはformatted()と指定子を使う
        return """
                **%s**
                記事のポイント
                %s

                [記事を読む](%s)

                
                """.formatted(title, summary, url);
    }
}
