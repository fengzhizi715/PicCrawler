package com.cv4j.piccrawler.strategy;

/**
 * Created by tony on 2017/10/13.
 */
public abstract class NormalStrategy implements FileStrategy {

    /**
     * 指定文件名
     * @return
     */
    public abstract String fileName();

    @Override
    public FileGenType genType() {
        return FileGenType.NORMAL;
    }
}
