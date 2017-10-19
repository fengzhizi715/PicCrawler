package com.cv4j.piccrawler.proxy;

import com.cv4j.piccrawler.proxy.domain.Proxy;

import java.util.List;

/**
 * Created by tony on 2017/10/19.
 */
public interface ProxyListPageParser {

    /**
     * 是否只要匿名代理
     */
    static final boolean anonymousFlag = true;
    List<Proxy> parse(String content);
}
