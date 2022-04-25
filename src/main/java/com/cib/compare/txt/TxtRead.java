package com.cib.compare.txt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TxtRead implements ApplicationRunner {

    @Value("${txt.preffix}")
    private String preffix;
    @Value("${txt.allPath}")
    private String allPath;
    @Value("${txt.outputPath}")
    private String outputPath;
    @Value("${company}")
    private String company;
    String md5 = null;
    String txtPath = null;
    String fileName = null;
    List<String> txtApPath = new ArrayList<>();
    String content;
    Queue queue = new LinkedBlockingQueue();
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE));

    @Override
    public void run(ApplicationArguments args) throws Exception {
        findTxt(allPath);
        log.info("=====所有的第一个txt已扫描完毕=====");
        for (String s : txtApPath) {
            readTxt(s);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                thread();
            }
        }).start();
        while(true){
            while(queue.size()>0){
                log.info("[{}][队列数据消费]当前队列数量:[{}]，正在处理...",Thread.currentThread().getName(),queue.size());
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        String filewaitingdeal = (String) queue.poll();
                        if(!StringUtils.hasText(filewaitingdeal)||filewaitingdeal==null){
                            return;
                        }
                    }
                });
            }
        }
    }


    //找第一个txt的路径
    public List<String> findTxt(String path) {
        //正则实现对指定字符串的截取
        Pattern pat = Pattern.compile("\\S+\\.txt");
        File file = new File(path);
        File[] arr = file.listFiles();
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                //如果是文件夹，再调用一下方法
                if (arr[i].isDirectory() || txtApPath.size() == 5000) {
                    findTxt(arr[i].getAbsolutePath());
                }
                Matcher mat = pat.matcher(arr[i].getAbsolutePath());
                //根据正则表达式，寻找匹配的文件
                if (mat.matches()) {
                    //返回第一个txt文件的String绝对路径并存入集合
                    String apPath = arr[i].getAbsolutePath();
                    txtApPath.add(apPath);
                }
            }
        }
        return txtApPath;
    }

    public void readTxt(String filePath) {
        StringBuilder lineTxt = new StringBuilder();

//            获取第一个txt信息

        try {
            File file = new File(filePath);
             if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
//              遍历txt内容
                while ((content = br.readLine()) != null) {
                    lineTxt.append(content).append("\n");
                    //添加txt路径与文件名至队列
                    //TODO 队列数量控制
                    queue.add(content);
                    //内部类中使用但未声明的任何局部变量必须在内部类的正文之前明确分配，需要重新赋值给另外一个变量
                    String finalContent = content;

                }
                br.close();
            } else {
                log.info("文件不存在!");
            }
        } catch (Exception e) {
            log.error("文件读取错误!{}",e);
        }

//        return queue;
    }


    public void thread(){
        //启用多线程
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String finalContent = (String) queue.poll();
                log.info("[{}]",Thread.currentThread().getName());
                //判断环境
                boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
                boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
                if (windows) {
                    //有工具
                    //指定分隔符进行分割,*需要转义
                    String[] line = finalContent.split(" [*]");
                    md5 = line[0];
                    Integer firstIndex = line[1].indexOf("\\");
                    Integer lastIndex = line[1].lastIndexOf("\\");
                    //截取文件名
                    fileName = line[1].substring(lastIndex + 1, line[1].length());
                    //截取相对路径
                    String fileRP = line[1].substring(firstIndex, lastIndex +1);
                    //拼接绝对路径
                    //  \\\\ ，java解析为\\交给正则表达式， 正则表达式再经过一次转换，把\\转换成为\
                    String fileAP = (fileRP + fileName).replaceAll("\\\\\\\\","\\\\");
                    log.info("windows下第二个txt绝对路径==>{}",fileAP);
                    md5CompareByTool(fileAP);
                } else if (linux) {
                    //无工具
                    //  /zsp/2017/01/20220408.txt
                    Integer firstIndex = finalContent.indexOf("/");
                    Integer lastIndex = finalContent.lastIndexOf("/");
                    //截取文件名
                    String txtName = finalContent.substring(firstIndex, lastIndex + 1);
                    //截取相对路径
                    txtPath = finalContent.replaceAll(txtName, "");
                    //拼接绝对路径
                    String fileAP = ((preffix + '/' + txtPath) + txtName).replaceAll("//", "/");
                    log.info("linux下第二个txt绝对路径==>{}",fileAP);
                    md5Compare(fileAP);
                }
            }
        });
    }

    /**
     * 非工具生成的txt
     *
     * @param lineTxt
     * @return
     */
    public boolean md5Compare(String lineTxt) {
        //          获取第二个txt内的信息
        Boolean flag = false;
        try {
            //指定分隔符进行分割
            String[] line = lineTxt.split(",,");
            md5 = line[0];
            Integer firstIndex = line[1].indexOf("/");
            Integer lastIndex = line[1].lastIndexOf("/");
            //截取文件名
            fileName = line[1].substring(lastIndex + 1, line[1].length());
            //截取相对路径
            String fileRP = line[1].substring(firstIndex, lastIndex + 1);
            //拼接绝对路径
            String fileAP = fileRP + fileName;
            //list.add(line[1]);
            //MD5比对
            Md5 getMD5 = new Md5();
            String newMD5 = getMD5.getMd5(fileAP);
            if (newMD5.equals(md5)) {
                //TODO 文件层级加一层厂商
                TxtWrite.writeFile((outputPath + "/" +company + "/" + "info.txt").replaceAll("//", "/").trim(), fileAP + "," + fileRP + "," + fileName);
            } else {
                TxtWrite.writeFile((outputPath + "/" +company +"/" + "error.txt").replaceAll("//", "/").trim(), fileAP + "," + fileRP + "," + fileName);
            }
            flag = true;
        } catch (Exception e) {
            log.error("[{}]md5校验失败!",fileName);
        }
        return flag;
    }

    /**
     * 工具生成的txt
     *
     * @param lineTxt
     * @return
     */
    public boolean md5CompareByTool(String lineTxt) {
        //3c89c64d591ab2eaf01fbd2253b3a623 *BaiDuYunCould\BaiduNetdisk\api-ms-win-core-console-l1-1-0.dll
//          获取第二个txt内的信息
        Boolean flag = false;
        try {
            //指定分隔符进行分割
            String[] line = lineTxt.split(" [*]");
            String newMd5 = line[0];
            Integer firstIndex = line[1].indexOf("\\");
            Integer lastIndex = line[1].lastIndexOf("\\");
            //截取文件名
            fileName = line[1].substring(lastIndex + 1, line[1].length());
            //截取相对路径
            String fileRP = line[1].substring(firstIndex, lastIndex + 1);
            //拼接绝对路径
            String fileAP = fileRP + fileName;
            //MD5比对
            if (newMd5.equals(md5)) {
                flag = true;
                TxtWrite.writeFile((outputPath + "\\" + "info.txt").replaceAll("\\\\\\\\", "\\\\").trim(), fileAP + "," + fileRP + "," + fileName);
            } else {
                TxtWrite.writeFile((outputPath + "\\" + "error.txt").replaceAll("\\\\\\\\", "\\").trim(), fileAP + "," + fileRP + "," + fileName);
            }
            flag = true;
        } catch (Exception e) {
            log.error("[{}]md5校验失败!",fileName);
        }
        return flag;

    }


}

