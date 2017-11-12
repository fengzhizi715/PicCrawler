package com.cv4j.piccrawler.http;

import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tony on 2017/10/27.
 */
public class HttpParam {

    @Getter
    private int timeOut;

    @Getter
    private HttpHost proxy;

    @Getter
    private BasicClientCookie cookie;

    @Getter
    private Map<String,String> header;

    private HttpParam(HttpParamBuilder builder) {
        this.timeOut = builder.timeOut;
        this.proxy = builder.proxy;
        this.cookie = builder.cookie;
        this.header = builder.header;
    }

    public static class HttpParamBuilder {

        private int timeOut;
        private HttpHost proxy;
        private BasicClientCookie cookie;
        private Map<String,String> header = new HashMap<>();

        public HttpParamBuilder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public HttpParamBuilder proxy(HttpHost proxy) {
            this.proxy = proxy;
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
