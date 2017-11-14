package com.cv4j.piccrawler.download.strategy;

/**
 * Created by tony on 2017/10/12.
 */
public abstract class AutoIncrementStrategy implements FileStrategy {

    /**
     * 子类可以重写该方法，指定自增长开始的数字
     * @return
     */
    public int start() {

        return 0;
    }

    public FileGenType genType(){

        return FileGenType.AUTO_INCREMENT;
    }
}
