package com.cv4j.piccrawler.domain;

import com.safframework.tony.common.utils.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHost;

/**
 * Created by tony on 2017/11/14.
 */
public class Proxy {

    private String ip;
    private int port;
    private String scheme;

    @Getter
    @Setter
    private int failureTimes;//请求失败次数

    public Proxy(String ip,int port) {
        this.ip = ip;
        this.port = port;
        this.scheme = "http";
    }

    public Proxy(String ip,int port,String scheme) {
        this.ip = ip;
        this.port = port;
        if (Preconditions.isBlank(scheme)) {
            this.scheme = "http";
        } else {
            this.scheme = scheme;
        }
    }

    /**
     * 是否丢弃代理
     * 失败次数大于３，丢弃
     */
    public boolean isDiscardProxy(){

        return failureTimes>3;
    }

    /**
     * 将Proxy转换成一个HttpHost对象
     * @return
     */
    public HttpHost toHttpHost() {

        return new HttpHost(ip,port,scheme);
    }

    @Override
    public String toString() {

        return "ip="+ip+",port="+port+",scheme="+scheme;
    }
}
