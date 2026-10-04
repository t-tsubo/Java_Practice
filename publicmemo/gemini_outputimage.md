# 期待される出力
```json
{
    "theme": "何について書かれた内容か",
    "key_takeaways": "記事の重要な部分",
    "target_audience": "対象の読者"
}
```
# 出力を定義するJsonSchema
```json
{
    // typeやproperties, requiredといったプロパティは
    // json schemaという構造を定義する言語で定義された名前
    // 入力形式を指定
    "type": "object",
    // このオブジェクトが持つプロパティ
    "properties": {
        "theme": {
            "type": "string",
            "description": "記事のテーマと使われている技術名を短い一文で。"
        },
        "key_takeaways": {
            "type": "stirng",
            "description": "記事の魅力と読むことで得られるメリットを短い一文で。"
        },
        "target_audience": {
            "type": "stirng",
            "description": "記事が想定している読者層を短い一文で。"
        },
    },
    // required: 必須
    // ここにある項目は必ず記入する
    "required": ["theme", "key_takeaways", "target_audience"]
}
```
# メモ
- **JSON Schemaとは**
    - JSONのデータに型をつけたり、バリデーションルールを定義したりできる
    - 仕様及び言語とあったけど正直よくわからない
    - 自由に書けるjsonに制約を持たせることで共通言語として使いやすくしてる？
- **geminiの構造化出力**
    - geminiは構造化出力を行うためのライブラリがある
    - 上記のjson schemaを送ることでその定義に沿った内容を出力してくれる