package com.portfolio.qiita_summary_notifier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.DiscordWebhookClient;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.GeminiApiClient;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.QiitaApiClient;
import com.portfolio.qiita_summary_notifier.service.QiitaNotificationService;

@SpringBootTest
class QiitaSummaryNotifierApplicationTests {
	@Autowired 
	QiitaApiClient qac;

	@Autowired 
	GeminiApiClient gac;

	@Autowired 
	DiscordWebhookClient dwc;

	@Autowired 
	QiitaNotificationService qnc;

	@Value("${discord.webhook.token}")
	String dwt;

	// API接続用のテストなので、本来はファイルを分けるべき
	// 現時点では開発者以外は実行できない(失敗する)ことに注意
	// @Test 
	// void canNotificationToDiscord() {
	// 	qnc.execute("tag:Java OR tag:spring", dwt);
	// }

	@Test 
	void mojibake() {
		System.out.println("文字化け直った？");
		System.out.println(System.getProperty("file.encoding"));
		System.out.println(java.nio.charset.Charset.defaultCharset());	
	}

	// DBからデータを取得して通知まで行くテスト作る
	// mapperクラスのメソッドで有効なwebhookを追加する
	// トランザクション管理ですぐにロールバックするようにすれば漏れることはない
}
