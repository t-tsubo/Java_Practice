package com.portfolio.qiita_summary_notifier.infrastructure.discord.dto;

import lombok.Getter;

@Getter 
public class DiscordWebhookRequestDto {
    
    private final String content;

    public DiscordWebhookRequestDto(String content) {
        this.content = content;
    }

    // ここに整形用のメソッドを作る？
    // そのメソッドをclientが呼び出して送る
}
