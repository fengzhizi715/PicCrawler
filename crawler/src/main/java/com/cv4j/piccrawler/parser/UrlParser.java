package com.cv4j.piccrawler.parser;

import com.safframework.tony.common.utils.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tony on 2017/11/15.
 */
@Slf4j
public class UrlParser implements PageParser<List<String>> {

    @Override
    public List<String> parse(Document doc) {

        List<String> urls = new ArrayList<>();

        Elements links = doc.select("a[href]");

        if (Preconditions.isNotBlank(links)) {

            for (Element src : links) {
                if (Preconditions.isNotBlank(src.attr("abs:href"))) {

                    String href = src.attr("abs:href");
                    log.info(href);
                    urls.add(href);
                }
            }
        }

        return urls;
    }
}
