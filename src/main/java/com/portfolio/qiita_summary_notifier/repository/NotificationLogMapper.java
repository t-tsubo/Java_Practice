package com.portfolio.qiita_summary_notifier.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper 
public interface NotificationLogMapper {
    // 通知した結果を登録するメソッド
    // インターフェースのpublicはいらないみたい？
    // 暗黙的にpublicはついている 暗黙的でも書いた方がいいものもあるので違いがわからない
    void insertLog(Integer settingId, String articleId);

    // 通知済みかを確認するためのメソッド
    boolean existsBySettingIdAndArticleId(
        // @Paramの名前はxmlに渡すときの名前
        // 基本的にJavaの名前に合わせる？
        @Param("settingId") Integer settingId,
        @Param("articleId") String articleId
    );
}
