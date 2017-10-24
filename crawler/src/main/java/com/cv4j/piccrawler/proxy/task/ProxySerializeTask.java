package com.cv4j.piccrawler.proxy.task;

import com.cv4j.piccrawler.Constant;
import com.cv4j.piccrawler.Utils;
import com.cv4j.piccrawler.proxy.ProxyPool;
import com.cv4j.piccrawler.proxy.domain.Proxy;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by tony on 2017/10/19.
 */
@Slf4j
public class ProxySerializeTask implements Runnable {

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

                    if (!p.isDiscardProxy()){
                        proxyArray[i++] = p;
                    }
                }
            } finally {
                ProxyPool.lock.readLock().unlock();
            }

            Utils.serializeObject(proxyArray, Constant.proxyPath);
            log.info("成功序列化" + proxyArray.length + "个代理");
        }
    }
}