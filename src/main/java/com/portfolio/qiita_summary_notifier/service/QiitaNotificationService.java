package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// このクラスでインフラの3つのメソッドを使って出力する
// ここで出力内容も整理すればいい...はず
@Service
@RequiredArgsConstructor
public class QiitaNotificationService {

    private final ArticleProvider articleProvider;
    private final NotificationSender notificationSender;
    private final Summarizer summarizer;

    public void execute(String tag, String webhookUrl) {

        List<Article> articles = articleProvider.getArticles(tag);
        
        for (Article article : articles) {
            String summary = summarizer.getSummaryOfArticle(article);
            notificationSender.excuteNotification(webhookUrl, summary);
        }
    }
}

// 今のままだと三回も通知を送るし、要約内容しか届かない
// タイトルとURLを要約内容と組み合わせたオブジェクト、
// 組み合わせるためのメソッドなどが必要
// geminiの返答内容もjson形式にした方が管理しやすい
