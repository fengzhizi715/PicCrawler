package com.cv4j.piccrawler.strategy;

import com.cv4j.piccrawler.FileStrategy;

/**
 * Created by tony on 2017/10/12.
 */
public abstract class AutoIncrementStrategy implements FileStrategy {

    public int start() {

        return 0;
    }
}
