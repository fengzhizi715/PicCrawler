package com.cv4j.piccrawler.parser;

import com.cv4j.piccrawler.utils.Utils;
import com.safframework.tony.common.utils.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tony on 2017/11/14.
 */
@Slf4j
public class PicParser implements PageParser<List<String>>{

    @Override
    public List<String> parse(Document doc) {

        List<String> urls = new ArrayList<>();

        Elements media = doc.select("[src]");

        if (Preconditions.isNotBlank(media)) {

            for (Element src : media) {
                if (src.tagName().equals("img")) {

                    if (Preconditions.isNotBlank(src.attr("abs:src"))) { // 图片的绝对路径不为空

                        String picUrl = src.attr("abs:src");
                        log.info(picUrl);
                        urls.add(picUrl);
                    } else if (Preconditions.isNotBlank(src.attr("src"))){ // 图片的相对路径不为空

                        String picUrl = src.attr("src").replace("//","");
                        picUrl = "http://"+ Utils.tryToEscapeUrl(picUrl);
                        log.info(picUrl);
                        urls.add(picUrl);
                    }
                }
            }
        }

        return urls;
    }
}
