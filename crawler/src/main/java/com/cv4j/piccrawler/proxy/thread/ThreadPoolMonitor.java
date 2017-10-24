package com.cv4j.piccrawler.proxy.thread;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工具类，监视ThreadPoolExecutor执行情况
 * Created by tony on 2017/10/19.
 */
@Slf4j
public class ThreadPoolMonitor implements Runnable {

    private ThreadPoolExecutor executor;
    private String name = "";
    public static volatile boolean isStopMonitor = false;

    public ThreadPoolMonitor(ThreadPoolExecutor executor, String name){
        this.executor = executor;
        this.name = name;
    }

    public void run(){
        while(!isStopMonitor){
            log.debug(name +
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, queueSize: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getQueue().size(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("InterruptedException",e);
            }
        }
    }
}
