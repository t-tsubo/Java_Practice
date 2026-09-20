package com.portfolio.qiita_summary_notifier.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class NotificationLog {

    private Integer id;

    private Integer settingId;

    private String articleId; 

    private LocalDateTime notifiedAt;
}
