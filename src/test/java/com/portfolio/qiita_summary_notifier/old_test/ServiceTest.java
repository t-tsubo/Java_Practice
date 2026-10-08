package com.portfolio.qiita_summary_notifier.old_test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.infrastructure.discord.DiscordWebhookClient;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.QiitaApiClient;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.ArticleWithSummary;
import com.portfolio.qiita_summary_notifier.service.QiitaNotificationService;

import lombok.extern.slf4j.Slf4j;


@Slf4j 
@SpringBootTest 
public class ServiceTest {

    @Value("${discord.webhook.token}")
    String discordWebhookToken;

    @Autowired 
    QiitaApiClient qiitaApiClient;

    @Autowired 
    QiitaNotificationService qiitaNotificationService;

    @Autowired 
    DiscordWebhookClient discordWebhookClient;

    @Test 
    void singleSettingNotificationTest() {
        // settingオブジェクトを作成
        NotificationSetting testSetting = new NotificationSetting();
        // 必要なデータだけsetterで入れていく
        testSetting.setId(1);
        testSetting.setIncludeTags("Java, spring");
        testSetting.setExcludeTags("ポエム , , MCP");
        testSetting.setWebhookUrl(discordWebhookToken);

        // settingをexecuteForSetting()に渡す
        qiitaNotificationService.deliverArticles(testSetting);

    }

    @Test 
    void Qiitaから記事を取得して正常に動くか確認() {
        List<Article> articles = qiitaApiClient.fetchArticles("Java, spring", "ポエム , , MCP");
        articles.forEach(article -> System.out.println(article.getTitle()));
    }

    @Test 
    void ArticleWithSummaryに正常にデータが格納されるか確認() {
        List<Article> articles = qiitaApiClient.fetchArticles("Java, spring", "ポエム , , MCP");
        try {
            File jsonFile = new File("logs\\cache\\sample.json");
            ObjectMapper objectMapper = new ObjectMapper();
            GeminiResponseDto geminiResponseDto = objectMapper.readValue(jsonFile, GeminiResponseDto.class);

            List<ArticleWithSummary> articleWithSummarys = new ArrayList<>();
            for (Article article : articles) {
                articleWithSummarys.add(new ArticleWithSummary(article, geminiResponseDto));
            }

            articleWithSummarys.forEach(aws -> System.out.println(aws));

            discordWebhookClient.notifyNewArticles(discordWebhookToken, articleWithSummarys);

        } catch (Exception e) {

        }
    }

    @Test 
    void DiscordのDtoとembedの確認() {
        
    }


}
