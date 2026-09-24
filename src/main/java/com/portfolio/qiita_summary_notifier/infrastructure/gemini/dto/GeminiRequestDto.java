package com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto;

import lombok.Getter;

// 今回は読み取りだけなので@Getterとした
@Getter
// geminiにリクエストするbody内容を管理している
public class GeminiRequestDto {
    
    private final String model;
    private final String input;

    public GeminiRequestDto(String prompt) {
        // 無料枠の中でRPMが15, RPDが500と、実用に耐える内容だったのでこれにした
        this.model = "gemini-3.5-flash-lite";
        this.input = prompt;
    }
}
