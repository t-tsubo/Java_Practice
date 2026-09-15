package com.portfolio.qiita_summary_notifier.service;

// Discordに通知を送るためのインターフェース
public interface NotificationSender {
    // 現時点ではString summaryとしている
    // 今後は要約だけでなく、タイトルとURLも追加したクラスを渡すかもしれない
    public void excuteNotification(String webhookUrl, String content);
}
