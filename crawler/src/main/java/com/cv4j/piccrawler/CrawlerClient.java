package com.cv4j.piccrawler;

import com.cv4j.piccrawler.strategy.AutoIncrementStrategy;
import com.cv4j.piccrawler.strategy.NormalStrategy;
import com.safframework.tony.common.utils.Preconditions;
import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tony on 2017/9/11.
 */
public class CrawlerClient {

    /**
     * 全局连接池对象
     */
    private static PoolingHttpClientConnectionManager connManager = null;
    private static AtomicInteger count = new AtomicInteger();
    private final static int BUFFER_SIZE = 0x2000; // 8192

    private int timeOut;
    private int repeat = 1;
    private int sleepTime = 0;
    private String userAgent;
    private String referer;
    private FileStrategy fileStrategy;
    private HttpHost proxy;

    /**
     * 配置连接池信息，支持http/https
     */
    static {
        SSLContext sslcontext = null;
        try {
            //获取TLS安全协议上下文
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }}, null);

            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(true)
                    .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
            Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", scsf).build();

            connManager = new PoolingHttpClientConnectionManager(sfr);

            // 设置最大连接数
            connManager.setMaxTotal(200);
            // 设置每个连接的路由数
            connManager.setDefaultMaxPerRoute(20);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private CrawlerClient() {
    }

    public static CrawlerClient get() {
        return Holder.CLIENT;
    }

    private static class Holder {
        private static final CrawlerClient CLIENT = new CrawlerClient();
    }

    /**
     * @param userAgent 添加User-Agent
     * @return
     */
    public CrawlerClient ua(String userAgent) {

        this.userAgent = userAgent;
        return this;
    }

    /**
     * @param referer
     * @return
     */
    public CrawlerClient referer(String referer) {

        this.referer = referer;
        return this;
    }

    /**
     * @param timeOut 设置超时时间
     * @return
     */
    public CrawlerClient timeOut(int timeOut) {

        this.timeOut = timeOut;
        return this;
    }

    /**
     * @param fileStrategy 设置生成文件的策略
     * @return
     */
    public CrawlerClient fileStrategy(FileStrategy fileStrategy) {

        this.fileStrategy = fileStrategy;
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
    public CrawlerClient proxy(HttpHost proxy) {

        this.proxy = proxy;
        return this;
    }

    /**
     * 获取Http客户端连接对象
     *
     * @param timeOut 超时时间
     * @return Http客户端连接对象
     */
    private CloseableHttpClient getHttpClient(int timeOut) {

        // 创建Http请求配置参数
        RequestConfig requestConfig = null;

        if (proxy!=null) {
            requestConfig = RequestConfig.custom()
                    // 获取连接超时时间
                    .setConnectionRequestTimeout(timeOut)
                    // 请求超时时间
                    .setConnectTimeout(timeOut)
                    // 响应超时时间
                    .setSocketTimeout(timeOut)
                    .setProxy(proxy)
                    .build();
        } else {
            requestConfig = RequestConfig.custom()
                    // 获取连接超时时间
                    .setConnectionRequestTimeout(timeOut)
                    // 请求超时时间
                    .setConnectTimeout(timeOut)
                    // 响应超时时间
                    .setSocketTimeout(timeOut)
                    .build();
        }

        // 创建httpClient
        return HttpClients.custom()
                // 把请求相关的超时信息设置到连接客户端
                .setDefaultRequestConfig(requestConfig)
                // 把请求重试设置到连接客户端
                .setRetryHandler(new RetryHandler())
                // 配置连接池管理对象
                .setConnectionManager(connManager)
                .build();
    }

    /**
     * 下载图片
     *
     * @param url 图片地址
     * @return
     */
    public void downloadPic(String url) {

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

    /**
     * 具体实现图片下载的方法
     *
     * @param url
     */
    private void doDownloadPic(String url) {

        try {
            writeImageToFile(createHttpWithPost(url));
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
    public Flowable<File> downloadPicToFlowable(String url) {

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
                    .map(s->createHttpWithPost(s))
                    .map(response->writeImageToFile(response));

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
                    .map(s->createHttpWithPost(s))
                    .observeOn(Schedulers.io())
                    .map(response->writeImageToFile(response));
        }

        return null;
    }

    /**
     * 下载多张图片
     * @param urls
     */
    public void downloadPics(List<String> urls) {

        if (Preconditions.isNotBlank(urls)) {
            urls.forEach(url->{

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

        Flowable.just(url)
                .map(s->createHttpWithGet(s))
                .map(response->parseHtmlToImages(response))
                .subscribe(urls->downloadPics(urls));
    }

    /**
     * 创建网络请求
     * @param url
     * @return
     */
    private CloseableHttpResponse createHttpWithPost(String url) {

        // 获取客户端连接对象
        CloseableHttpClient httpClient = getHttpClient(timeOut);
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);

        if (Preconditions.isNotBlank(userAgent)) {
            httpPost.addHeader("User-Agent",userAgent);
        }

        if (Preconditions.isNotBlank(referer)) {
            httpPost.addHeader("Referer",referer);
        }

        CloseableHttpResponse response = null;

        // 执行请求
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private CloseableHttpResponse createHttpWithGet(String url) {

        // 获取客户端连接对象
        CloseableHttpClient httpClient = getHttpClient(timeOut);
        // 创建Get请求对象
        HttpGet httpGet = new HttpGet(url);

        if (Preconditions.isNotBlank(userAgent)) {
            httpGet.addHeader("User-Agent",userAgent);
        }

        if (Preconditions.isNotBlank(referer)) {
            httpGet.addHeader("Referer",referer);
        }

        CloseableHttpResponse response = null;

        // 执行请求
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 将response的响应流写入文件中
     * @param response
     * @return
     * @throws IOException
     */
    private File writeImageToFile(CloseableHttpResponse response) throws IOException{

        if (response==null) return null;

        // 获取响应实体
        HttpEntity entity = response.getEntity();

        if (entity==null) return null;

        InputStream is = entity.getContent();

        // 包装成高效流
        BufferedInputStream bis = new BufferedInputStream(is);

        if (fileStrategy == null) {
            fileStrategy = new FileStrategy() {

                @Override
                public String filePath() {
                    return "images";
                }

                @Override
                public String picFormat() {
                    return "png";
                }

                @Override
                public FileGenType genType() {

                    return FileGenType.RANDOM;
                }
            };
        }

        String path = fileStrategy.filePath();
        String format = fileStrategy.picFormat();
        FileGenType fileGenType = fileStrategy.genType();

        File directory = null;
        // 写入本地文件
        if (Preconditions.isNotBlank(path)) {

            directory = new File(path);
            if (!directory.exists()) {

                if (path.contains("/")) {
                    directory.mkdirs();
                } else {
                    directory.mkdir();
                }

                if (!directory.exists() || !directory.isDirectory()) {

                    directory = Utils.mkDefaultDir(directory);
                }
            }
        } else {
            directory = Utils.mkDefaultDir(directory);
        }

        String fileName = null;
        switch (fileGenType) {

            case RANDOM:

                fileName = Utils.randomUUID();
                break;

            case AUTO_INCREMENT:

                if (fileStrategy instanceof AutoIncrementStrategy) {

                    if (count.get()< ((AutoIncrementStrategy) fileStrategy).start()) {
                        count.set(((AutoIncrementStrategy) fileStrategy).start());
                    }
                }

                count.incrementAndGet();
                fileName = String.valueOf(count.get());
                break;

            case NORMAL:

                fileName = ((NormalStrategy)fileStrategy).fileName();
                break;
        }

        File file = new File(directory, fileName + "." + format);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        byte[] byt = new byte[BUFFER_SIZE];
        Integer len = -1;
        while ((len = bis.read(byt)) != -1) {
            bos.write(byt, 0, len);
        }

        bos.close();
        bis.close();

        if (response != null) {
            try {
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException e) {
                System.err.println("释放链接错误");
                e.printStackTrace();
            }
        }

        return file;
    }

    private List<String> parseHtmlToImages(CloseableHttpResponse response) {

        // 获取响应实体
        HttpEntity entity = response.getEntity();

        InputStream is = null;
        try {
            is = entity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String html = Utils.inputStream2Str(is);

        Document doc = Jsoup.parse(html);

        Elements media = doc.select("[src]");
        List<String> urls = new ArrayList<>();

        for (Element src : media) {
            if (src.tagName().equals("img")) {

                if (Preconditions.isNotBlank(src.attr("abs:src"))) {

                    urls.add(src.attr("abs:src"));
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