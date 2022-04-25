package com.cib.compare.txt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Lee
 */
@Component
@Slf4j
public class Md5 {
    public String getMd5(String path){
        String newMD5 = null;
        try {
            newMD5 = DigestUtils.md5Hex(String.valueOf(new FileInputStream(path)));
        } catch (FileNotFoundException e) {
            log.error("[找不到文件md5{}]",e);
        }
        return newMD5;
    }

}
