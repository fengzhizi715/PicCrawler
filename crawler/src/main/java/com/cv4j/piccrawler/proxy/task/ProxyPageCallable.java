package com.cv4j.piccrawler.proxy.task;

import com.cv4j.piccrawler.Page;
import com.cv4j.piccrawler.proxy.ProxyHttpClient;
import com.cv4j.piccrawler.proxy.ProxyListPageParser;
import com.cv4j.piccrawler.proxy.ProxyPool;
import com.cv4j.piccrawler.proxy.domain.Proxy;
import com.cv4j.piccrawler.proxy.site.ProxyListPageParserFactory;
import com.safframework.tony.common.utils.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by tony on 2017/10/25.
 */
@Slf4j
public class ProxyPageCallable implements Callable<List<Proxy>>{

    protected String url;
    private ProxyHttpClient proxyHttpClient = null;

    public ProxyPageCallable(String url){
        this.url = url;
        this.proxyHttpClient = ProxyHttpClient.get();
    }

    @Override
    public List<Proxy> call() throws Exception {

        long requestStartTime = System.currentTimeMillis();
        HttpGet tempRequest = null;
        try {
            Page page = proxyHttpClient.getWebPage(url);
            int status = page.getStatusCode();
            long requestEndTime = System.currentTimeMillis();
            String logStr = Thread.currentThread().getName() + " "  +
                    "  executing request " + page.getUrl()  + " response statusCode:" + status +
                    "  request cost time:" + (requestEndTime - requestStartTime) + "ms";
            if(status == HttpStatus.SC_OK){
                log.debug(logStr);
                return handle(page);
            }

        } catch (IOException e) {

        } finally {
            if (tempRequest != null){
                tempRequest.releaseConnection();
            }
        }

        return null;
    }

    /**
     * 将下载的proxy放入代理池
     * @param page
     */
    private List<Proxy> handle(Page page){

        if (page == null || Preconditions.isBlank(page.getHtml())){
            return null;
        }

        List<Proxy> result = new ArrayList<>();

        ProxyListPageParser parser = ProxyListPageParserFactory.getProxyListPageParser(ProxyPool.proxyMap.get(url));
        if (parser!=null) {

            List<Proxy> proxyList = parser.parse(page.getHtml());
            if(Preconditions.isNotBlank(proxyList)) {

                for(Proxy p : proxyList){

                    boolean containFlag = ProxyPool.list.contains(p);
                    if (!containFlag) {
                        result.add(p);
                    }
                }
            }

        }

        return result;
    }
}
