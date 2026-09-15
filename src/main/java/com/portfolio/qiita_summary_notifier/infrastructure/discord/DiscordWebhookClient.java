package com.portfolio.qiita_summary_notifier.infrastructure.discord;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.dto.DiscordWebhookRequestDto;
import com.portfolio.qiita_summary_notifier.service.NotificationSender;
import com.portfolio.qiita_summary_notifier.utility.Utility;

@Component
public class DiscordWebhookClient implements NotificationSender {
    
    private final RestClient restClient;

    public DiscordWebhookClient() {
        
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        restClient = RestClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    @Override
    public void excuteNotification(String webhookUrl, String content) {
        DiscordWebhookRequestDto request = new DiscordWebhookRequestDto(content);

        try {
            restClient.post()
                    .uri(webhookUrl)
                    .body(request)
                    .retrieve()
                    // 今回の通知のように、何も戻ってこないタイプのリクエストは
                    // このメソッドをつけないといけない
                    // エラーにはならないみたいだが送信ができていなかった(?)
                    .toBodilessEntity();

            Utility.writeUtf8Text("logs/notificationLog.txt", "送信成功: " + request);
        } catch (RestClientException e) {
            Utility.writeUtf8Text("logs/notificationLog.txt", "送信失敗" + e.getMessage());
        }
    }
}
