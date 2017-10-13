package com.cv4j.piccrawler.strategy;

import com.cv4j.piccrawler.FileGenType;
import com.cv4j.piccrawler.FileStrategy;

/**
 * Created by tony on 2017/10/13.
 */
public abstract class NormalStrategy implements FileStrategy {

    public abstract String fileName();

    @Override
    public FileGenType genType() {
        return FileGenType.NORMAL;
    }
}
