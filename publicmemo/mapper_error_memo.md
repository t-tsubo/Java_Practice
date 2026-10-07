# 概要
マッパークラスでエラーが起きたので原因をメモしておく  
結論としては@Paramの付け忘れだった
# エラーの内容
```txt
org.mybatis.spring.MyBatisSystemException: 
### Error updating database.  
    -- 中略 --
Cause: org.apache.ibatis.binding.BindingException: Parameter 'settingId' not found. Available parameters are [arg1, arg0, param1, param2]
Cause: org.apache.ibatis.binding.BindingException: Parameter 'settingId' not found. Available parameters are [arg1, arg0, param1, param2]
    -- 中略 --
at com.portfolio.qiita_summary_notifier.service.QiitaNotificationService.deliverArticles(QiitaNotificationService.java:114)
at com.portfolio.qiita_summary_notifier.ServiceTest.singleSettingNotificationTest(ServiceTest.java:51)
```
# エラーの読み方
- 一行目はおおもとの例外
    - 基本これだけ見ても情報量は足りない
- Causeはなぜ例外となったのかの理由が書いてある
    - 基本的にここを見る
    - 今回はxmlファイル(NotificationLogMapper.xml)で`${settingId}`を参照しようとしたが、その名前がmybatisに渡されておらずにエラーとなった
- atはエラーの起きた場所
    - 大半はライブラリの場所が出てきてよくわからないが、一部自分のコードの行を書かれていることがある
    - 今回では`com.portfolio.qiita_summary_notifier. ...`の部分
    - せめてどこで起こっているかわかれば、その処理が何を呼び出したり実行しているのかを見て判断の材料にできる
# 今回の詳細
- 前述のとおりxml側が`${settingId}`を解決できずエラーとなった
- mybatisで@Mapperを使って結び付けるときは、Java側の引数に対して`@Param`で名前を付ける必要がある
- 名前を付けないとarg0やparam1といった機械的な名前になってしまう
# 行った対策
```Java
// 修正前
void insertLog(Integer settingId, String articleId);
// 修正後: @Paramで明示的に名前をバインド
void insertLog(
    @Param("settingId") Integer settingId, 
    @Param("articleId") String articleId
);
```