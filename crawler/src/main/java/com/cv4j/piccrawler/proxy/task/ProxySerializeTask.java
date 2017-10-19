package com.cv4j.piccrawler.proxy.task;

import com.cv4j.piccrawler.Constant;
import com.cv4j.piccrawler.Utils;
import com.cv4j.piccrawler.proxy.ProxyHttpClient;
import com.cv4j.piccrawler.proxy.ProxyPool;
import com.cv4j.piccrawler.proxy.domain.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tony on 2017/10/19.
 */
public class ProxySerializeTask implements Runnable {

    Logger logger=  LoggerFactory.getLogger(ProxySerializeTask.class);

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000 * 60 * 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Proxy[] proxyArray = null;
            ProxyPool.lock.readLock().lock();
            try {
                proxyArray = new Proxy[ProxyPool.proxySet.size()];
                int i = 0;
                for (Proxy p : ProxyPool.proxySet){
                    if (!Utils.isDiscardProxy(p)){
                        proxyArray[i++] = p;
                    }
                }
            } finally {
                ProxyPool.lock.readLock().unlock();
            }

            Utils.serializeObject(proxyArray, Constant.proxyPath);
            logger.info("成功序列化" + proxyArray.length + "个代理");
        }
    }
}
