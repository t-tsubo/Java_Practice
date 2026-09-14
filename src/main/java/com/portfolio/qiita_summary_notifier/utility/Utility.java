package com.portfolio.qiita_summary_notifier.utility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Utility {
    // インスタンス化されないように隠蔽
    private Utility() {};

    /**
     * 指定したファイル名で、テキストをUTF-8で保存する。
     * @param fileName 保存するファイル名(例: "log.txt")
     * @param content  保存したい文字列
     */
    
    public static void writeUtf8Text(String fileName, String content) {
        try {
            Path filePath = Paths.get(fileName);

            Files.writeString(
                filePath,                               // 入力したいファイルのパス(名前)
                content,                                // ファイルに書き込みたい内容(文字列)
                StandardCharsets.UTF_8,                 // 文字コードを指定
                StandardOpenOption.CREATE,              // ファイルがなければ新規作成
                StandardOpenOption.TRUNCATE_EXISTING    // ファイルがあれば中身を消して上書き
            );

            // 成功したときにパスを出力
            System.out.println("File output successful: " + filePath.toAbsolutePath());
        
        } catch (IOException e) {
            System.out.println("File output faild: " + e.getMessage());
        }


    }

    // StandardCharsetsは文字コードを打ち間違えないように
    // 定数で管理するクラス enumではない

    // StandardOpenOptionはファイル入出力時に使いたいオプションを
    // enum形でまとめたクラス

    // streamで文字列をファイルに出力する
    // nio.2と呼ばれる新しい入出力方法？
    // 詳しく調べていない

    // javadocは使われているメソッド名にカーソルを重ねると内容が出てくるらしい
    // 思ったより見やすい 使っていったほうがいいかも
}
