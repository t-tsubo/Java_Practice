package com.portfolio.qiita_summary_notifier.infrastructure.discord.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiscordWebhookRequestDto {

    // Botの名前（Webhookの設定を上書きして好きな名前にできます）
    private String username = "Qiita通知Bot";
    // 埋め込みカードのリスト（今回は1記事につき1つ入れます）
    private List<Embed> embeds = new ArrayList<>();
    
    // Embedを追加するための便利メソッド
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
