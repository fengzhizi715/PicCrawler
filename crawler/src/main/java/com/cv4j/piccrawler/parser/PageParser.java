package com.cv4j.piccrawler.parser;

import org.jsoup.nodes.Document;

/**
 * Created by tony on 2017/11/14.
 */
public interface PageParser<T> {

    /**
     * parse html
     *
     * @param html page html data
     *
     */
    T parse(Document html);
}
