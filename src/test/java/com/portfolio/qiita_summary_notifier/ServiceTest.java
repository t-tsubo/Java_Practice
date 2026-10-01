package com.portfolio.qiita_summary_notifier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.service.QiitaNotificationService;



@SpringBootTest 
public class ServiceTest {

    @Value("${discord.webhook.token}")
    String discordWebhookToken;

    @Autowired 
    QiitaNotificationService qiitaNotificationService;



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
}
