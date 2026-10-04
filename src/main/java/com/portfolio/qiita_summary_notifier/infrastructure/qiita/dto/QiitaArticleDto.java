package com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

// QiitaAPIから受け取ったjsonから必要な情報のみを格納するクラス
// このアノテーションで、受け取った値(jsonファイル)から
// ここにあるフィールドの値だけ格納する
@JsonIgnoreProperties (ignoreUnknown = true)
@Data
@NoArgsConstructor 
public class QiitaArticleDto {
    private String title;
    private String url;
    private String body;
    private String id;  // 重複管理に必要 Qiitaの記事idは文字列入り

    private User user;
    private List<Tag> tags;



    @JsonIgnoreProperties (ignoreUnknown = true)
    @Data 
    @NoArgsConstructor 
    public static class User {
        private String id;
        private String name;
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    @Data 
    @NoArgsConstructor 
    public static class Tag {
        private String name;
    }
}
