package com.portfolio.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

// RetryTemplateを作るための設定クラス
@Configuration
public class RetryConfig {
    
    /**memo: RetryTemplateを作る
     * まずは@BeanでDIコンテナにオブジェクトを用意する
     * 最終的に返すRetryTemplateを作るための流れ
     * RetryPolicy: templateの設定を定義するクラス
     *              buildパターンを使って設定を追加していく
     * maxRetries:  リトライ回数の最大
     * includes:    キャッチする例外     
     * excludes:    キャッチしない例外
     * delay:       最初にリトライするまでのディレイ
     * multiplier:  再試行時に遅延時間を増やすときの指数
     * maxDelay:    ディレイの最大値、増えすぎないようにするため
     */
    @Bean
    public RetryTemplate qiitaRetryTemplate() {
        return new RetryTemplate(
            RetryPolicy.builder()
                    .maxRetries(3)
                    .includes(ResourceAccessException.class, HttpServerErrorException.class)
                    .excludes(HttpClientErrorException.class)
                    .delay(Duration.ofSeconds(2))
                    .multiplier(2.0)
                    .maxDelay(Duration.ofSeconds(10))
                    .build()
        );
    }
}
