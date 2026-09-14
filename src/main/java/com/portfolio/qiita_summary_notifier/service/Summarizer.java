package com.portfolio.qiita_summary_notifier.service;

// geminiに要約してもらうためのインターフェース
public interface Summarizer {
    // 記事を一つ与えて、要約された文章(String)を返す
    // 戻ってくる要約内容はプロンプトによってフォーマットが変わるので、
    // 整形などは別のメソッドで定義する
    // このメソッドはあくまで、geminiに記事を投げて
    // 要約を取得するためにAPIをたたくだけのメソッド
    public String getSummaryOfArticle(Article article);
}
