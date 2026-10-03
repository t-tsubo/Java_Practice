package com.portfolio.qiita_summary_notifier.infrastructure.gemini.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import lombok.extern.slf4j.Slf4j;

// デバッグ用のjsonファイルを管理するクラス
@Slf4j 
public class SummaryCache {

    private final String CACHE_DIR = "./logs/cache/";

    public String check

    private void saveCacheOfSummaryJson(String articleId, String summaryJson) {
        try {
            Path filePath = Paths.get(CACHE_DIR, articleId + ".json");

            Files.writeString(
                filePath, 
                summaryJson, 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE
            );

        } catch(IOException e) {
            log.error("cache生成エラー: {}", e);

        }
    }

    private String getSummaryCache(String articleId) {
        try {
            Path filePath = Paths.get(CACHE_DIR, articleId + ".json");

            if (!Files.exists(filePath)) {

            }
        }
    }
}
