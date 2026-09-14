package com.portfolio.qiita_summary_notifier.service;

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
}
