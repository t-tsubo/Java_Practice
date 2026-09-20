package com.portfolio.qiita_summary_notifier.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class NotificationSetting {

    private Integer id;

    private Integer userId;

    private String searchKeywords;

    private String exclusionKeywords;

    private String webhookUrl;

    private String webhookName;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
