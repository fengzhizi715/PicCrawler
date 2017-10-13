package com.cv4j.piccrawler;

/**
 * Created by tony on 2017/10/10.
 */

public enum FileGenType {

    RANDOM,          // 基于uuid来随机生成文件名
    AUTO_INCREMENT,  // 通过自增长的方式来生成文件名
    NORMAL           // 生成指定的文件名，此策略不适合调用repeat()来重复抓取同一个url
}
