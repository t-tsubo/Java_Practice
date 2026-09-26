package com.portfolio.qiita_summary_notifier.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {

    private Integer id;

    private String username;

    private String passwordHash;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
