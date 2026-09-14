package com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

// jsonの階層構造は内部クラスを使って表現する
/* 一部省略
{
  "steps": [
    {
      "content": [
        {
          "text": "AI learns patterns from data, then uses those patterns to make predictions or decisions on new data."
        }
      ]
    }
  ]
}
*/

// Spring boot配下のRestClientはデフォルトで未知のフィールド
// java側で記述していないjsonのプロパティなどを
// 自動的に無視する設定になっているらしい？
@JsonIgnoreProperties(ignoreUnknown = true)
// Jacksonはリフレクション機能？を使ってprivateフィールドに
// 直接値を代入できるらしい
// なのでDataではなくGetterのみでもいい
@Data
@NoArgsConstructor 
public class GeminiResponseDto {
    // 一番上の階層にあるstepsプロパティ
    private List<Step> steps;

    // stepsプロパティ(配列)の中身
    @NoArgsConstructor 
    @Data
    public static class Step {
        // contentプロパティは配列になっている
        private List<Content> content;
    } 

    // contentプロパティ(配列)の中身
    @NoArgsConstructor 
    @Data
    public static class Content {
        // content配列の中のtextプロパティの値
        // geminiからの返答がここに格納される
        private String text;
    }

    // DTOから文字列(要約)を返すためのメソッド
    // ガード節で、要約された内容を取得できなければ失敗を返す
    public String getSummaryText() {
        if (steps == null || steps.isEmpty()) {
            return "要約失敗";
        }
        // インデックスを直接指定しているので悪いコード
        // jsonの中が変化しても対応できるように修正が必要
        // MVPのため、とりあえずこれで進める
        List<Content> contents = steps.get(1).getContent();
        if (contents == null || contents.isEmpty()) {
            return "要約失敗";
        }

        String text = contents.get(0).getText();
        if (text == null || text.isBlank()) {
            return "要約失敗";
        }

        return text;

        // Optionalというnull安全性を確認するクラスがあるらしい
        // return Optional.ofNullable(steps)
        //     .filter(s -> !s.isEmpty())
        //     .map(s -> s.get(0).getContent())
        //     .filter(c -> c != null && !c.isEmpty())
        //     .map(c -> c.get(0).getText())
        //     .filter(t -> t != null && !t.isBlank())
        //     .orElse("要約失敗");
    }

    

}
