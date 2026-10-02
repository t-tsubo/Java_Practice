# 期待される出力
```json
{
    "summary_points": [
        "要約1行目",
        "要約2行目",
        "要約3行目"
    ],
    "knowledge_level": "必要な知識レベル"
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
        // summary_pointsというjsonオブジェクトの定義
        "summary_points": {
            // 中身は配列型
            "type": "array",
            // 配列の要素はstring型
            "items": { "type": "string" },
            // 配列の最小値・最大値を指定
            "minItems": 3,
            "maxItems": 3,
            // この要素に入れてほしいものの説明
            "description": "3つの要点をそれぞれ1文で簡潔に抽出してください"
        },
        // knowledge_levelというjsonオブジェクトの定義
        "knowledge_level": {
            "type": "string",
            "description": "この記事を読むために必要な知識レベルや内容を1文で簡潔に記述してください"
        }
    },
    // required: 必須
    // summary_pointsとknowledge_levelというプロパティは必ず用意するように要請している
    "required": ["summary_points", "knowledge_level"]
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