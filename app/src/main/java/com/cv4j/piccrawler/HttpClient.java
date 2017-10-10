package com.cv4j.piccrawler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tony on 2017/10/2.
 */
public class HttpClient {



    public static void main(String[] args){

        for (int i=0;i<100;i++) {

            CrawlerClient.get()
                    .timeOut(6000)
                    .fileStrategy(new FileStrategy() {

                        @Override
                        public String filePath() {
                            return "temp";
                        }

                        @Override
                        public String picFormat() {
                            return "png";
                        }

                        @Override
                        public FileGenType genType() {

                            return FileGenType.AUTO_INCREMENT;
                        }
                    })
                    .downloadPic("http://www1.10086.cn/jsp/common/image.jsp?r=0.400457732767749");
        }
    }
}
