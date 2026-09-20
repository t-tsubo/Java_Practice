-- DROP TABLE テーブルを削除する
-- IF EXISTS テーブルがなくてもエラーにならない(警告メッセージだけ)
-- 依存関係に注意 先にlogsから消す
DROP TABLE IF EXISTS notification_logs;
DROP TABLE IF EXISTS notification_settings;
DROP TABLE IF EXISTS users;

-- 1. ユーザーのアカウント情報（認証・個人情報）
-- 今後emailなども追加されるかもしれないので、usersテーブルとして分離
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 通知のルール設定（アプリのコア設定）
-- どういう条件で通知するか、どこに通知するかがわかるテーブル
CREATE TABLE notification_settings (
    id SERIAL PRIMARY KEY,
    -- ON DELETE CASCADE usersのidが消えたらこのレコードごと消す
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, 
    -- 妥協案で、タグをカンマ区切りで受け取る
    -- 本来は正規化しないといけないが、まずは動くことを優先する
    search_keywords VARCHAR(255),
    exclusion_keywords VARCHAR(255),
    -- まずは1設定1URL 
    -- 複数URLを設定する場合、レコードの内容を複製することになる
    webhook_url VARCHAR(255) NOT NULL,
    webhook_name VARCHAR(255),
    -- 通知切り替え用 
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 通知の送信履歴（重複チェック用）
-- ここにあるレコードとマッチしたら通知しない(setting_idとarticle_idを見る？)
-- ログテーブルなので、何もしないと肥大化する
-- 1レコードで焼く100Byte未満だが注意が必要
CREATE TABLE notification_logs (
    id SERIAL PRIMARY KEY,
    -- どの設定で通知を行ったか
    setting_id INTEGER REFERENCES notification_settings(id) ON DELETE CASCADE,
    -- どの記事を通知したか
    article_id VARCHAR(255) NOT NULL,
    notified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);