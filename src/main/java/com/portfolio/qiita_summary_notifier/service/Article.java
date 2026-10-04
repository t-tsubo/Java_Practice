package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto.Tag;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Article {
    private String title;
    private String url;
    private String body;
    private String id;
    private User user;
    private List<Tag> tags;
}
