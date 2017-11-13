package com.cv4j.piccrawler.http;

import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tony on 2017/10/27.
 */
public class HttpParam {

    @Getter
    private int timeOut;

    @Getter
    private BasicClientCookie cookie;

    @Getter
    private Map<String,String> header;

    private List<HttpHost> proxyPool;

    private AtomicInteger index = new AtomicInteger();

    private HttpParam(HttpParamBuilder builder) {
        this.timeOut = builder.timeOut;
        this.proxyPool = builder.proxyPool;
        this.cookie = builder.cookie;
        this.header = builder.header;
    }

    /**
     * 采用round robin算法来获取Proxy
     * @return
     */
    public HttpHost getProxy(){

        HttpHost result = null;

        if (getProxyPoolSize() > 0) {

            if (index.get() > proxyPool.size()-1) {
                index.set(0);
            }

            result = proxyPool.get(index.get());
            index.incrementAndGet();
        }

        return result;
    }

    /**
     * 获取代理池中代理的数量，如果大于1则在HttpManager中开启代理池的功能
     * @return
     */
    public int getProxyPoolSize() {

        return proxyPool != null ? proxyPool.size() : 0;
    }

    public static class HttpParamBuilder {

        private int timeOut;
        private BasicClientCookie cookie;
        private List<HttpHost> proxyPool = new ArrayList<>();
        private Map<String,String> header = new HashMap<>();

        public HttpParamBuilder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public HttpParamBuilder addProxy(HttpHost proxy) {
            this.proxyPool.add(proxy);
            return this;
        }

        public HttpParamBuilder cookie(BasicClientCookie cookie) {
            this.cookie = cookie;
            return this;
        }

        public HttpParamBuilder addHeader(String name,String value) {
            header.put(name,value);
            return this;
        }

        public String getHeader(String name) {
            return header.get(name);
        }

        public HttpParam build() {
            return new HttpParam(this);
        }
    }
}
