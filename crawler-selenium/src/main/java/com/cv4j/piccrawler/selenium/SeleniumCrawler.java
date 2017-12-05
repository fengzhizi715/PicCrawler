package com.cv4j.piccrawler.selenium;

import com.cv4j.piccrawler.CrawlerClient;
import com.cv4j.piccrawler.download.strategy.FileGenType;
import com.cv4j.piccrawler.download.strategy.FileStrategy;
import com.cv4j.piccrawler.parser.PicParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

/**
 * Created by tony on 2017/12/5.
 */
public class SeleniumCrawler {

    private PicParser picParser = new PicParser();

    private CrawlerClient crawlerClient = CrawlerClient.get()
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
            .build();

    static {
        System.setProperty("webdriver.chrome.driver", "crawler-selenium/chromedriver");
    }

    public void downloadPic(String url) {

        WebDriver driver = new ChromeDriver();

        driver.get(url);
        String html = driver.getPageSource();
        List<String> urls = parseHtmlToImages(html,picParser);
        crawlerClient.downloadPics(urls);
    }

    /**
     *
     * @param url
     * @param scrollDownNum 模拟鼠标滚动到屏幕底部到次数
     */
    public void downloadPic(String url,int scrollDownNum) {

        WebDriver driver = new ChromeDriver();

        driver.get(url);
        String html = driver.getPageSource();
        List<String> urls = parseHtmlToImages(html,picParser);

        crawlerClient.downloadPics(urls);

        if (scrollDownNum>1) {

            for (int i=0;i<scrollDownNum-1;i++) {

                Utils.scrollDown(driver);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                html = driver.getPageSource();

                urls = parseHtmlToImages(html,picParser);

                crawlerClient.downloadPics(urls);
            }
        }
    }

    /**
     * 将response进行解析，解析出图片的url，存放到List中
     * @param html
     * @param picParser
     * @return
     */
    private List<String> parseHtmlToImages(String html, PicParser picParser) {

        Document doc = Jsoup.parse(html);

        List<String> urls = picParser.parse(doc);

        return urls;
    }
}
