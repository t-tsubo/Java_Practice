package com.portfolio.qiita_summary_notifier.infrastructure.discord.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**memo: discordドキュメントの見方
 * 
 * 
 * 
 * tips: たいていのドキュメントは?が任意を表す
 * Fieldに?がたくさんついてたけど、すべてあってもなくてもいい
 */
@Data
@NoArgsConstructor
public class DiscordWebhookRequestDto {

    // Botの名前 (指定が可能)
    private String username = "Qiita通知Bot";
    private List<Embed> embeds = new ArrayList<>();
    
    // 1通知で複数のembedを出力するためのメソッド
    public void addEmbed(Embed embed) {
        this.embeds.add(embed);
    }

    // --- ここから下はインナークラス ---

    @Data
    @NoArgsConstructor
    public static class Embed {
        private String title;
        private String url;
        private Integer color; // 10進数で指定 (例: Qiitaの緑は 5620992)
        private Author author;
        private List<Field> fields = new ArrayList<>();
        private Footer footer;

        public void addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
        }
    }

    @Data
    @NoArgsConstructor
    public static class Author {
        private String name;

        public Author(String name) {
            this.name = name;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Field {
        private String name;
        private String value;
        private boolean inline; // 横に並べるかどうか

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Footer {
        private String text;

        public Footer(String text) {
            this.text = text;
        }
    }
}
