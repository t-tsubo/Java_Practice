package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

// qiitaの記事を取得するインターフェース
public interface ArticleProvider {
    public List<Article> getArticles(String tag);
}
