package com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data 
@NoArgsConstructor 
public class GeminiResponseDto {
    @JsonProperty("theme")
    private String theme;
    
    @JsonProperty("key_takeaways")
    private String keyTakeaways;

    @JsonProperty("target_audience") 
    private String targetAudience;

}
