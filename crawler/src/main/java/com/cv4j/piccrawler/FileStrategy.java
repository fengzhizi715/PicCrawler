package com.cv4j.piccrawler;

/**
 * 文件策略，包含生成的路径、格式、文件名的生成策略
 * Created by tony on 2017/10/10.
 */
public interface FileStrategy {

    /**
     * 文件存放路径
     * @return
     */
    String filePath();

    /**
     * 图片保存的格式
     * @return
     */
    String picFormat();

    /**
     * 文件生成的策略
     * @return
     */
    FileGenType genType();
}
