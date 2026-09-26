package com.portfolio.qiita_summary_notifier.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;

@Mapper 
public interface NotificationSettingMapper {
    // 設定の新規登録
    void insertSetting(NotificationSetting setting);

    // アクティブな設定の検索 リストで返す
    // 通知を送るときに使う
    List<NotificationSetting> selectActiveSettings();

    // ユーザーごとの設定一覧を取得
    List<NotificationSetting> selectByUserId(@Param("userId") Integer userId);
    // 設定の検索(1件のみ) 詳細表示などで使う？
    NotificationSetting selectById(@Param("id") Integer id);

    // 設定の更新
    void updateSetting(NotificationSetting setting);
    // 設定の削除
    void deleteSetting(@Param("id") Integer id);

    // 将来的にisActiveだけ切り替えるボタンも追加する
}
