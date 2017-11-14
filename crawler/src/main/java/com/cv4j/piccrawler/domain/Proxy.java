package com.cv4j.piccrawler.domain;

/**
 * Created by tony on 2017/11/14.
 */
public class Proxy {

    private String ip;
    private int port;
    private String scheme;
    private int failureTimes;//请求失败次数

    /**
     * 是否丢弃代理
     * 失败次数大于３，丢弃
     */
    public boolean isDiscardProxy(){

        if(failureTimes >= 3){
            return true;
        }
        return false;
    }
}
