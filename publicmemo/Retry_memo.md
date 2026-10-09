# 概要
例外が発生しても何度かリトライしたい処理がある(APIへのリクエストなど)  
それを解決するための方法をメモする  
基本的には、  
1. @Retryable
2. RetryTemplate  
この二つを使えばいいはず  
# Retryable
- 今までは別のライブラリだったものがspringframework7以降で吸収された？
- アノテーションをつけて管理するらしい
# RetryTemplate
- 