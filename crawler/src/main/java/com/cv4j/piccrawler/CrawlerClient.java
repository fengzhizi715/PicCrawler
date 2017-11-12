package com.cv4j.piccrawler;

import com.cv4j.piccrawler.download.DownloadManager;
import com.cv4j.piccrawler.http.HttpManager;
import com.cv4j.piccrawler.http.HttpParam;
import com.cv4j.piccrawler.utils.Utils;
import com.safframework.tony.common.utils.IOUtils;
import com.safframework.tony.common.utils.Preconditions;
import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by tony on 2017/9/11.
 */
@Slf4j
public class CrawlerClient {

    private int repeat = 1;
    private int sleepTime = 0;
    private HttpManager httpManager;
    private DownloadManager downloadManager;
    private HttpParam.HttpParamBuilder httpParamBuilder = new HttpParam.HttpParamBuilder();
    private boolean isWebPage = false;

    private CrawlerClient() {

        httpManager = HttpManager.get();
        downloadManager = DownloadManager.get();
    }

    public static CrawlerClient get() {

        return new CrawlerClient();
    }

    /******************* CrawlerClient 的配置 Start *******************／

    /**
     * @param userAgent 添加User-Agent
     * @return
     */
    public CrawlerClient ua(String userAgent) {

        addHeader("User-Agent",userAgent);
        return this;
    }

    /**
     * @param referer
     * @return
     */
    public CrawlerClient referer(String referer) {

        addHeader("Referer",referer);
        return this;
    }

    /**
     * @param timeOut 设置超时时间
     * @return
     */
    public CrawlerClient timeOut(int timeOut) {

        httpParamBuilder.timeOut(timeOut);
        return this;
    }

    /**
     * @param fileStrategy 设置生成文件的策略
     * @return
     */
    public CrawlerClient fileStrategy(FileStrategy fileStrategy) {

        downloadManager.setFileStrategy(fileStrategy);
        return this;
    }

    /**
     * @param repeat 设置重复次数
     * @return
     */
    public CrawlerClient repeat(int repeat) {

        if (repeat > 0) {
            this.repeat = repeat;
        }

        return this;
    }

    /**
     * @param sleepTime 每次请求url时先sleep一段时间，单位是milliseconds
     * @return
     */
    public CrawlerClient sleep(int sleepTime) {

        if (sleepTime > 0) {
            this.sleepTime = sleepTime;
        }

        return this;
    }

    /**
     *
     * @param proxy 代理的host
     * @return
     */
    public CrawlerClient addProxy(HttpHost proxy) {

        httpParamBuilder.addProxy(proxy);
        return this;
    }

    /**
     *
     * @param cookie 设置浏览器的cookie
     * @return
     */
    public CrawlerClient cookie(BasicClientCookie cookie) {

        httpParamBuilder.cookie(cookie);
        return this;
    }

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public CrawlerClient addHeader(String name,String value) {

        httpParamBuilder.addHeader(name,value);
        return this;
    }

    public CrawlerClient build() {

        httpManager.setHttpParam(httpParamBuilder.build());
        return this;
    }

    /******************* CrawlerClient 的配置 End *******************／

    /**
     * 下载图片
     *
     * @param url 图片地址
     * @return
     */
    public void downloadPic(String url) {

        if (isWebPage) {

            if (sleepTime>0) {

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }

            doDownloadPic(url);

        } else {

            for (int i = 0; i < repeat; i++) {

                if (sleepTime>0) {

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }

                doDownloadPic(url);
            }
        }
    }

    /**
     * 具体实现图片下载的方法
     *
     * @param url
     */
    private void doDownloadPic(String url) {

        try {

            if (Preconditions.isNotBlank(httpParamBuilder.getHeader("Referer"))) { // 针对需要Referer的图片，我们使用Get请求

                downloadManager.writeImageToFile(httpManager.createHttpWithGet(url),url);
            } else {
                downloadManager.writeImageToFile(httpManager.createHttpWithPost(url),url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载图片
     *
     * @param url 图片地址
     * @return
     */
    public void downloadPicUseRx(String url) {

        Flowable<File> flowable = downloadPicToFlowable(url);

        if (flowable!=null) {

            flowable.subscribe();
        }
    }

    /**
     * 下载图片
     *
     * @param url 图片地址
     * @return
     */
    public Flowable<File> downloadPicToFlowable(final String url) {

        if (repeat==1) {

            return Flowable.create((FlowableEmitter<String> e) -> {

                if (sleepTime>0) {

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }

                e.onNext(url);

            }, BackpressureStrategy.BUFFER)
                    .map(s->httpManager.createHttpWithPost(s))
                    .map(response->downloadManager.writeImageToFile(response,url));

        } else if (repeat>1) {
            return Flowable.create((FlowableEmitter<String> e) -> {

                for (int i = 0; i < repeat; i++) {

                    if (sleepTime>0) {

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }
                    }

                    e.onNext(url);
                }

            }, BackpressureStrategy.BUFFER)
                    .map(s->httpManager.createHttpWithPost(s))
                    .observeOn(Schedulers.io())
                    .map(response->downloadManager.writeImageToFile(response,url));
        }

        return null;
    }

    /**
     * 下载多张图片
     * @param urls
     */
    public void downloadPics(List<String> urls) {

        if (Preconditions.isNotBlank(urls)) {
            urls.stream().parallel().forEach(url->{

                try {
                    CompletableFuture.runAsync(() -> downloadPic(url)).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 下载整个网页的全部图片
     * @param url
     */
    public void downloadWebPageImages(String url) {

        if (Preconditions.isNotBlank(url)) {

            isWebPage = true;

            Flowable.just(url)
                    .map(s->httpManager.createHttpWithGet(s))
                    .map(response->parseHtmlToImages(response))
                    .subscribe(urls -> downloadPics(urls),
                            throwable-> System.out.println(throwable.getMessage()));
        }
    }

    /**
     * 下载多个网页的全部图片
     * @param urls
     */
    public void downloadWebPageImages(List<String> urls) {

        if (Preconditions.isNotBlank(urls)) {

            isWebPage = true;

            Flowable.fromIterable(urls)
                    .parallel()
                    .map(url->httpManager.createHttpWithGet(url))
                    .map(response->parseHtmlToImages(response))
                    .sequential()
                    .subscribe(list -> downloadPics(list),
                            throwable-> System.out.println(throwable.getMessage()));
        }
    }

    private List<String> parseHtmlToImages(CloseableHttpResponse response) {

        // 获取响应实体
        HttpEntity entity = response.getEntity();

        InputStream is = null;
        String html = null;

        try {
            is = entity.getContent();
            html = IOUtils.inputStream2String(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document doc = Jsoup.parse(html);

        Elements media = doc.select("[src]");
        List<String> urls = new ArrayList<>();

        if (Preconditions.isNotBlank(media)) {

            for (Element src : media) {
                if (src.tagName().equals("img")) {

                    if (Preconditions.isNotBlank(src.attr("abs:src"))) { // 图片的绝对路径不为空

                        String picUrl = src.attr("abs:src");
                        log.info(picUrl);
                        urls.add(picUrl);
                    } else if (Preconditions.isNotBlank(src.attr("src"))){ // 图片的相对路径不为空

                        String picUrl = src.attr("src").replace("//","");
                        picUrl = "http://"+Utils.tryToEscapeUrl(picUrl);
                        log.info(picUrl);
                        urls.add(picUrl);
                    }
                }
            }
        }

        if (response != null) {
            try {
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException e) {
                System.err.println("释放链接错误");
                e.printStackTrace();
            }
        }

        return urls;
    }
 }