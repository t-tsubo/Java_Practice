package com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto;

import lombok.Getter;

// 今回は読み取りだけなので@Getterとした
@Getter
// geminiにリクエストするbody内容を管理している
public class GeminiRequestDto {
    
    private final String model;
    private final String input;

    public GeminiRequestDto(String prompt) {
        this.model = "gemini-3.6-flash";
        this.input = prompt;
    }
}
