-- テストデータ作る
-- users(仮)
INSERT INTO users (username, password_hash, updated_at)
VALUES 
('user1', 'password', CURRENT_TIMESTAMP), 
('user2', 'password', CURRENT_TIMESTAMP);

-- notification_settings(仮)
INSERT INTO notification_settings (
    user_id, 
    search_keywords, 
    exclusion_keywords, 
    webhook_url, 
    webhook_name,
    is_active
)
VALUES 
(1, 'Java,Spring', 'AI','https://discord.com/api/webhooks/test1', 'ch1', true),
(2, 'Python,Django', '機械学習', 'https://discord.com/api/webhooks/test2', 'ch2', false);

-- notification_log(仮)
INSERT INTO notification_log (setting_id, article_id)
VALUES (1, 'test_article_id')

