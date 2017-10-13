package com.cv4j.piccrawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by tony on 2017/10/10.
 */
public class Utils {

    /**
     * 生成随机数<br>
     * GUID： 即Globally Unique Identifier（全球唯一标识符） 也称作 UUID(Universally Unique
     * IDentifier) 。
     * <p>
     * 所以GUID就是UUID。
     * <p>
     * GUID是一个128位长的数字，一般用16进制表示。算法的核心思想是结合机器的网卡、当地时间、一个随即数来生成GUID。
     * <p>
     * 从理论上讲，如果一台机器每秒产生10000000个GUID，则可以保证（概率意义上）3240年不重复。
     *
     * @return
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    public static String inputStream2Str(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        try {
            while ((len = in.read(b)) > 0) {
                out.write(b, 0, len);
            }
            return out.toString("utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 创建默认的文件夹用于存放图片
     * @param directory
     */
    public static File mkDefaultDir(File directory) {

        directory = new File("images");
        if (!directory.exists()) {
            directory.mkdir();
        }

        return directory;
    }
}
