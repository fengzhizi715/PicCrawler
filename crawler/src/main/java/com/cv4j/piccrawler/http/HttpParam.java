package com.cv4j.piccrawler.http;

import com.cv4j.piccrawler.domain.Proxy;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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

    @Getter
    private boolean autoReferer;

    private List<Proxy> proxyPool;

    private AtomicInteger index = new AtomicInteger();

    private HttpParam(HttpParamBuilder builder) {
        this.timeOut = builder.timeOut;
        this.proxyPool = builder.proxyPool;
        this.cookie = builder.cookie;
        this.header = builder.header;
        this.autoReferer = builder.autoReferer;
    }

    /**
     * 采用round robin算法来获取Proxy
     * @return
     */
    public Proxy getProxy(){

        Proxy result = null;

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
        private List<Proxy> proxyPool = new CopyOnWriteArrayList<>();
        private Map<String,String> header = new HashMap<>();
        private boolean autoReferer = false;

        public HttpParamBuilder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public HttpParamBuilder addProxy(Proxy proxy) {
            this.proxyPool.add(proxy);
            return this;
        }

        public HttpParamBuilder addProxyPool(List<Proxy> proxyList) {
            this.proxyPool.addAll(proxyList);
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

        public HttpParamBuilder autoReferer() {
            autoReferer = true;
            return this;
        }

        public boolean isAutoReferer() {
            return autoReferer;
        }

        public String getHeader(String name) {
            return header.get(name);
        }

        public HttpParam build() {
            return new HttpParam(this);
        }
    }
}
