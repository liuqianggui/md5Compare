package com.cib.compare.txt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TxtWrite {
    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @throws IOException
     */
    public static void writeFile(String filePath, String content) throws IOException {
        //true表示追加内容
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true));
        bufferedWriter.write(content);
        //换行
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
}
