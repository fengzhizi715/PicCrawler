package com.cv4j.piccrawler.parser;

import org.jsoup.nodes.Document;

/**
 * Created by tony on 2017/11/14.
 */
public interface PageParser {

    /**
     * parse html
     *
     * @param html page html data
     *
     */
    void parse(Document html);
}
