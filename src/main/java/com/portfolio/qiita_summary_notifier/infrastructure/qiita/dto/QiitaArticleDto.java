package com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

// QiitaAPIから受け取ったjsonから必要な情報のみを格納するクラス
// このアノテーションで、受け取った値(jsonファイル)から
// ここにあるフィールドの値だけ格納する
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class QiitaArticleDto {
    private String title;

    private String url;

    private String body;
}
