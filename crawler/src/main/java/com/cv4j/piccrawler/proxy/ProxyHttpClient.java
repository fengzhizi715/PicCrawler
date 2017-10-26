package com.cv4j.piccrawler.proxy;

import com.cv4j.piccrawler.HttpManager;
import com.cv4j.piccrawler.Page;

import com.cv4j.piccrawler.proxy.domain.Proxy;
import com.cv4j.piccrawler.proxy.task.ProxyPageCallable;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by tony on 2017/10/25.
 */
@Slf4j
public class ProxyHttpClient {

    private ProxyHttpClient() {
    }

    public static ProxyHttpClient get() {
        return ProxyHttpClient.Holder.PROXY_HTTP_CLIENT;
    }

    private static class Holder {
        private static final ProxyHttpClient PROXY_HTTP_CLIENT = new ProxyHttpClient();
    }

    /**
     * 抓取代理
     */
    public void start() {

        List<Proxy> result = ProxyPool.proxyMap.keySet()
                .stream()
                .parallel()
                .map(new Function<String, List<Proxy>>() {

                    @Override
                    public List<Proxy> apply(String s)  {

                        try {
                            return new ProxyPageCallable(s).call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                })
                .flatMap(new Function<List<Proxy>, Stream<Proxy>>() {
                    @Override
                    public Stream<Proxy> apply(List<Proxy> proxies) {

                        if (proxies == null) return null;

                        return proxies.stream().parallel().filter(new Predicate<Proxy>() {
                            @Override
                            public boolean test(Proxy proxy) {

                                HttpHost httpHost = new HttpHost(proxy.getIp(), proxy.getPort());
                                return HttpManager.get().checkProxy(httpHost);
                            }
                        });
                    }
                })
                .collect(Collectors.toList());
    }

    public Page getWebPage(String url) throws IOException {
        return getWebPage(url, "UTF-8");
    }

    public Page getWebPage(String url, String charset) throws IOException {
        Page page = new Page();
        CloseableHttpResponse response = HttpManager.get().getResponse(url);
        if (response!=null) {
            page.setStatusCode(response.getStatusLine().getStatusCode());
            page.setUrl(url);
            try {
                if(page.getStatusCode() == 200){
                    page.setHtml(EntityUtils.toString(response.getEntity(), charset));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return page;
    }
}
