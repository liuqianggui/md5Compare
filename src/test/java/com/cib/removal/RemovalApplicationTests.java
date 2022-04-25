package com.cib.compare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.regex.Matcher;

@SpringBootTest
class RemovalApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
//        String finalContent = "3c89c64d591ab2eaf01fbd2253b3a623 *BaiDuYunCould\\BaiduNetdisk\\api-ms-win-core-console-l1-1-0.dll";
//        String f = finalContent.replaceAll("[*]","");
//        String[] line = finalContent.split(" ");
//        System.out.println(line[0]);

        String sb = "aaa\\\\bbb\\ccc\\txt.txt";
        String sbb = sb.replaceAll("\\\\\\\\","\\\\");
        System.out.println(sb);
        System.out.println(sbb);
    }
}
