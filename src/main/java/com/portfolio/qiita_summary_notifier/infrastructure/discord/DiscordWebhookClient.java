package com.portfolio.qiita_summary_notifier.infrastructure.discord;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.dto.DiscordWebhookRequestDto;
import com.portfolio.qiita_summary_notifier.service.ArticleWithSummary;
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
    public void notifyNewArticles(String webhookUrl, List<ArticleWithSummary> unformattedDataList) {
        
        /**memo: 送信の一連の流れ
         * まずDtoを使って受け取った生のコンテンツを整形する
         * それをRestClientのbodyに渡して送信する
         */
        DiscordWebhookRequestDto contents = createEmbed(unformattedDataList);

        try {
            restClient.post()
                    .uri(webhookUrl)
                    .body(contents)
                    .retrieve()
                    // 戻ってくる要素がないときにこのメソッドが必要
                    .toBodilessEntity();

        } catch (RestClientException e) {
            System.out.println("送信失敗: " + e.getMessage());
        }
    }

    private DiscordWebhookRequestDto createEmbed(List<ArticleWithSummary> unformattedDataList) {
        /**memo: Dtoを使って記事データと要約情報を組み立てる
         * 最終的にDtoを返して、それをRestClientのbodyに渡せばいいはず
         */
        DiscordWebhookRequestDto contents = new DiscordWebhookRequestDto();

        for (ArticleWithSummary article : unformattedDataList) {
            // インナークラスをインスタンス化
            DiscordWebhookRequestDto.Embed embed = new DiscordWebhookRequestDto.Embed();

            embed.setTitle(article.getTitle());
            embed.setUrl(article.getUrl());
            embed.setColor(5620992);

            // 投稿者名をauthorとして作成(文字列フォーマットで作成、逆にわかりずらい？)
            DiscordWebhookRequestDto.Author author = new DiscordWebhookRequestDto.Author
                (String.format("%s(%s)", article.getUsername(), article.getUserId()));
            embed.setAuthor(author);

            embed.addField("テーマ", article.getTheme(), false);
            embed.addField("ポイント", article.getKeyTakeaways(), false);
            embed.addField("読者層", article.getTargetAudience(), false);

            DiscordWebhookRequestDto.Footer footer = 
                new DiscordWebhookRequestDto.Footer(String.join(", ", article.getTags()));
            embed.setFooter(footer);

            contents.addEmbed(embed);
        }

        return contents;
    }
}
