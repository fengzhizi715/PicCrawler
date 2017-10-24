package com.cv4j.piccrawler.proxy;


import com.cv4j.piccrawler.Constant;
import com.cv4j.piccrawler.HttpManager;
import com.cv4j.piccrawler.Page;
import com.cv4j.piccrawler.Utils;
import com.cv4j.piccrawler.proxy.domain.Proxy;
import com.cv4j.piccrawler.proxy.task.ProxyPageTask;
import com.cv4j.piccrawler.proxy.task.ProxySerializeTask;
import com.cv4j.piccrawler.proxy.thread.SimpleThreadPoolExecutor;
import com.cv4j.piccrawler.proxy.thread.ThreadPoolMonitor;
import com.safframework.tony.common.utils.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tony on 2017/10/19.
 */
@Slf4j
public class ProxyHttpClient {

    /**
     * 代理网站下载线程池
     */
    private ThreadPoolExecutor proxyDownloadThreadExecutor;

    private boolean stopProxyCrawler = false;

    private ProxyHttpClient() {

        initThreadPool();
        initProxy();
    }

    public static ProxyHttpClient get() {
        return ProxyHttpClient.Holder.PROXY_HTTP_CLIENT;
    }

    private static class Holder {
        private static final ProxyHttpClient PROXY_HTTP_CLIENT = new ProxyHttpClient();
    }

    /**
     * 初始化线程池
     */
    private void initThreadPool(){

        proxyDownloadThreadExecutor = new SimpleThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), "proxyDownloadThreadExecutor");

        // 监测proxyDownloadThreadExecutor
        new Thread(new ThreadPoolMonitor(proxyDownloadThreadExecutor, "ProxyDownloadThreadExecutor")).start();
    }

    /**
     * 初始化proxy
     *
     */
    private void initProxy(){
        Proxy[] proxyArray = null;
        try {
            proxyArray = (Proxy[]) Utils.deserializeObject(Constant.proxyPath);
            int usableProxyCount = 0;

            if (Preconditions.isNotBlank(proxyArray)) {
                for (Proxy p : proxyArray){

                    if (p == null){
                        continue;
                    }

                    p.setTimeInterval(Constant.TIME_INTERVAL);
                    p.setFailureTimes(0);
                    p.setSuccessfulTimes(0);
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - p.getLastSuccessfulTime() < 1000 * 60 *60){
                        //上次成功离现在少于一小时
                        ProxyPool.proxyQueue.add(p);
                        ProxyPool.proxySet.add(p);
                        usableProxyCount++;
                    }
                }
                log.info("反序列化proxy成功，" + proxyArray.length + "个代理,可用代理" + usableProxyCount + "个");
            } else {
                log.info("没有可以反序列化的proxy");
            }

        } catch (Exception e) {
            log.warn("反序列化proxy失败");
        }
    }

    /**
     * 抓取代理
     */
    public void start() {

        // 抓取代理
        new Thread(() -> {

            while (true) {

                for (String url : ProxyPool.proxyMap.keySet()) {

                    if (isStopProxyCrawler()) break;

                    /**
                     * 首次本机直接下载代理页面
                     */
                    proxyDownloadThreadExecutor.execute(new ProxyPageTask(url, false));

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(1000 * 60 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 序列化代理
        new Thread(new ProxySerializeTask()).start();
    }

    public Page getWebPage(String url) throws IOException {
        return getWebPage(url, "UTF-8");
    }

    public Page getWebPage(String url, String charset) throws IOException {
        Page page = new Page();
        CloseableHttpResponse response = HttpManager.get().getResponse(url);
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
        return page;
    }

    public Page getWebPage(HttpRequestBase request) throws IOException {
        CloseableHttpResponse response = null;
        response = HttpManager.get().getResponse(request);
        Page page = new Page();
        page.setStatusCode(response.getStatusLine().getStatusCode());
        page.setHtml(EntityUtils.toString(response.getEntity()));

        page.setUrl(request.getURI().toString());
        return page;
    }

    public ThreadPoolExecutor getProxyDownloadThreadExecutor() {
        return proxyDownloadThreadExecutor;
    }

    public boolean isStopProxyCrawler() {
        return stopProxyCrawler;
    }

    public void setStopProxyCrawler(boolean stopProxyCrawler) {
        this.stopProxyCrawler = stopProxyCrawler;
    }
}
