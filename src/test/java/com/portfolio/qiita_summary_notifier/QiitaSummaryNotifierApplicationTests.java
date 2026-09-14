package com.portfolio.qiita_summary_notifier;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.DiscordWebhookClient;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.GeminiApiClient;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.QiitaApiClient;
import com.portfolio.qiita_summary_notifier.service.Article;

@SpringBootTest
class QiitaSummaryNotifierApplicationTests {
	@Autowired 
	QiitaApiClient qac;

	@Autowired 
	GeminiApiClient gac;

	@Autowired 
	DiscordWebhookClient dwc;

	@Value("${discord.webhook.token}")
	String dwt;

	// API接続用のテストなので、本来はファイルを分けるべき
	// 現時点では開発者以外は実行できない(失敗する)ことに注意
	@Test 
	void canNotificationToDiscord() {
		List<Article> articles = qac.getArticles("Java");
		for (Article article : articles) {
			String summary = gac.getSummaryOfArticle(article);
			dwc.excuteNotification(dwt, summary);
		}

	}
}
