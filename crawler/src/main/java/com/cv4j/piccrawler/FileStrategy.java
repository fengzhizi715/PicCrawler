package com.cv4j.piccrawler;

/**
 * 文件策略，包含生成的路径、格式、文件名的生成策略
 * Created by tony on 2017/10/10.
 */
public interface FileStrategy {

    String filePath();

    String picFormat();

    FileGenType genType();
}
