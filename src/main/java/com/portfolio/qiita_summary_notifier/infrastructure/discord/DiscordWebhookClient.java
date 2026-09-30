package com.portfolio.qiita_summary_notifier.infrastructure.discord;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.dto.DiscordWebhookRequestDto;
import com.portfolio.qiita_summary_notifier.service.NotificationSender;

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
    public void notifyNewArticles(String webhookUrl, String content) {
        DiscordWebhookRequestDto request = new DiscordWebhookRequestDto(content);

        try {
            restClient.post()
                    .uri(webhookUrl)
                    .body(request)
                    .retrieve()
                    // 戻ってくる要素がないときにこのメソッドが必要
                    .toBodilessEntity();

        } catch (RestClientException e) {
            System.out.println("送信失敗: " + e.getMessage());
        }
    }
}
