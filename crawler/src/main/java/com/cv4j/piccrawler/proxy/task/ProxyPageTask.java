package com.cv4j.piccrawler.proxy.task;

import com.cv4j.piccrawler.Constant;
import com.cv4j.piccrawler.HttpManager;
import com.cv4j.piccrawler.Page;
import com.cv4j.piccrawler.proxy.ProxyHttpClient;
import com.cv4j.piccrawler.proxy.ProxyListPageParser;
import com.cv4j.piccrawler.proxy.ProxyPool;
import com.cv4j.piccrawler.proxy.domain.Proxy;
import com.cv4j.piccrawler.proxy.site.ProxyListPageParserFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.cv4j.piccrawler.proxy.ProxyPool.proxyQueue;

/**
 * Created by tony on 2017/10/19.
 */
public class ProxyPageTask implements Runnable {

    Logger logger=  LoggerFactory.getLogger(ProxyPageTask.class);

    protected String url;
    private boolean proxyFlag;//是否通过代理下载
    private Proxy currentProxy;//当前线程使用的代理

    protected static ProxyHttpClient proxyHttpClient = ProxyHttpClient.get();

    private ProxyPageTask(){
    }

    public ProxyPageTask(String url, boolean proxyFlag){
        this.url = url;
        this.proxyFlag = proxyFlag;
    }

    public void run(){
        long requestStartTime = System.currentTimeMillis();
        HttpGet tempRequest = null;
        try {
            Page page = null;
            if (proxyFlag){
                tempRequest = new HttpGet(url);
                currentProxy = proxyQueue.take();

                HttpHost proxy = new HttpHost(currentProxy.getIp(), currentProxy.getPort());
                tempRequest.setConfig(HttpManager.get().getRequestConfigBuilder().setProxy(proxy).build());

                page = proxyHttpClient.getWebPage(tempRequest);
            }else {
                page = proxyHttpClient.getWebPage(url);
            }
            page.setProxy(currentProxy);
            int status = page.getStatusCode();
            long requestEndTime = System.currentTimeMillis();
            String logStr = Thread.currentThread().getName() + " " + getProxyStr(currentProxy) +
                    "  executing request " + page.getUrl()  + " response statusCode:" + status +
                    "  request cost time:" + (requestEndTime - requestStartTime) + "ms";
            if(status == HttpStatus.SC_OK){
                logger.debug(logStr);
                handle(page);
            } else {
                logger.error(logStr);
                Thread.sleep(100);
                retry();
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            retry();
        } finally {
            if(currentProxy != null){
                currentProxy.setTimeInterval(Constant.TIME_INTERVAL);
                proxyQueue.add(currentProxy);
            }
            if (tempRequest != null){
                tempRequest.releaseConnection();
            }
        }
    }

    /**
     * retry
     */
    public void retry(){
        proxyHttpClient.getProxyDownloadThreadExecutor().execute(new ProxyPageTask(url, false));
    }

    public void handle(Page page){

        if (page.getHtml() == null || page.getHtml().equals("")){
            return;
        }

        ProxyListPageParser parser = ProxyListPageParserFactory.getProxyListPageParser(ProxyPool.proxyMap.get(url));
        List<Proxy> proxyList = parser.parse(page.getHtml());
        for(Proxy p : proxyList){

            // TODO:
//            if(!ZhiHuHttpClient.getInstance().getDetailListPageThreadPool().isTerminated()){
                ProxyPool.lock.readLock().lock();
                boolean containFlag = ProxyPool.proxySet.contains(p);
                ProxyPool.lock.readLock().unlock();
                if (!containFlag){
                    ProxyPool.lock.writeLock().lock();
                    ProxyPool.proxySet.add(p);
                    ProxyPool.lock.writeLock().unlock();
                }
//            }
        }
    }

    private String getProxyStr(Proxy proxy){
        if (proxy == null){
            return "";
        }

        return proxy.getIp() + ":" + proxy.getPort();
    }
}
