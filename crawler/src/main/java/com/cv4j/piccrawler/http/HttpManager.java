package com.cv4j.piccrawler.http;

import com.cv4j.piccrawler.domain.Proxy;
import com.cv4j.piccrawler.utils.Utils;
import com.safframework.tony.common.utils.Preconditions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
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
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by tony on 2017/10/19.
 */
@Slf4j
public class HttpManager {

    /**
     * 全局连接池对象
     */
    private static PoolingHttpClientConnectionManager connManager = null;
    private CloseableHttpClient httpClient;

    @Setter
    private HttpParam httpParam;

    private boolean useProxyPool = false;

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

    private HttpManager() {
    }

    public static HttpManager get() {
        return HttpManager.Holder.MANAGER;
    }

    /**
     * 获取Http客户端连接对象
     * @return Http客户端连接对象
     */
    private CloseableHttpClient createHttpClient() {

        return createHttpClient(20000,null,null);
    }

    /**
     * 获取Http客户端连接对象
     * @param timeOut 超时时间
     * @param proxy   代理
     * @param cookie  Cookie
     * @return Http客户端连接对象
     */
    private CloseableHttpClient createHttpClient(int timeOut,HttpHost proxy,BasicClientCookie cookie) {

        // 创建Http请求配置参数
        RequestConfig.Builder builder = RequestConfig.custom()
                // 获取连接超时时间
                .setConnectionRequestTimeout(timeOut)
                // 请求超时时间
                .setConnectTimeout(timeOut)
                // 响应超时时间
                .setSocketTimeout(timeOut)
                .setCookieSpec(CookieSpecs.STANDARD);

        if (proxy!=null) {
            builder.setProxy(proxy);
        }

        RequestConfig requestConfig = builder.build();

        // 创建httpClient
        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        httpClientBuilder
                // 把请求相关的超时信息设置到连接客户端
                .setDefaultRequestConfig(requestConfig)
                // 把请求重试设置到连接客户端
                .setRetryHandler(new RetryHandler())
                // 配置连接池管理对象
                .setConnectionManager(connManager);

        if (cookie!=null) {
            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(cookie);
            httpClientBuilder.setDefaultCookieStore(cookieStore);
        }

        return httpClientBuilder.build();
    }


    /**
     * 创建网络请求 post请求
     * @param url
     * @return
     */
    public CloseableHttpResponse createHttpWithPost(String url) {

        // 获取客户端连接对象
        CloseableHttpClient httpClient = getHttpClient();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);

        if (Preconditions.isNotBlank(httpParam)) {

            boolean autoReferer = httpParam.isAutoReferer();

            Map<String,String> header = httpParam.getHeader();

            if (Preconditions.isNotBlank(header)) {

                if (autoReferer && !header.containsKey("Referer")) {

                    header.put("Referer", Utils.getReferer(url));
                }

                for (String key : header.keySet()) {
                    httpPost.setHeader(key,header.get(key));
                }
            }
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

    /**
     * 创建网络请求 get请求
     * @param url
     * @return
     */
    public CloseableHttpResponse createHttpWithGet(String url) {

        // 获取客户端连接对象
        CloseableHttpClient httpClient = getHttpClient();
        // 创建Get请求对象
        HttpGet httpGet = new HttpGet(url);

        if (Preconditions.isNotBlank(httpParam)) {

            boolean autoReferer = httpParam.isAutoReferer();

            Map<String,String> header = httpParam.getHeader();

            if (Preconditions.isNotBlank(header)) {

                if (autoReferer && !header.containsKey("Referer")) {

                    header.put("Referer", Utils.getReferer(url));
                }

                for (String key : header.keySet()) {
                    httpGet.setHeader(key,header.get(key));
                }
            }
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
     * 检测代理是否可用
     * @param proxy
     * @return
     */
    private boolean checkProxy(HttpHost proxy) {

        if (proxy == null) return false;

        Socket socket = null;
        try {
            socket = new Socket();
            InetSocketAddress endpointSocketAddr = new InetSocketAddress(proxy.getHostName(), proxy.getPort());
            socket.connect(endpointSocketAddr, 3000);
            return true;
        } catch (IOException e) {
//            logger.warn("FAILRE - CAN not connect!  remote: " + p);
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
//                    logger.warn("Error occurred while closing socket of validating proxy", e);
                }
            }
        }
    }

    private CloseableHttpClient getHttpClient() {

        if (useProxyPool) {
            // 使用多个代理的情况

            if (Preconditions.isNotBlank(httpParam)) {

                int timeOut = httpParam.getTimeOut();
                Proxy proxy = httpParam.getProxy(); // 随机取出代理
                BasicClientCookie cookie = httpParam.getCookie();

                if (proxy!=null) {
                    HttpHost httpHost = proxy.toHttpHost();
                    boolean check = checkProxy(httpHost);
                    if (check) { // 代理检测成功，使用代理
                        log.info("proxy："+proxy.toString()+" 代理可用");
                        httpClient = createHttpClient(timeOut,httpHost,cookie);
                    } else {
                        log.info("proxy："+proxy.toString()+" 代理不可用");
                        proxy.setFailureTimes(proxy.getFailureTimes()+1);
                        if (proxy.isDiscardProxy()) {
                            log.info("proxy："+proxy.toString()+"被丢弃");
                            httpParam.getProxyPool().remove(proxy);
                        }
                        httpClient = createHttpClient(timeOut,null,cookie);
                    }
                } else {
                    httpClient = createHttpClient(timeOut,null,cookie);
                }
            } else {
                httpClient = createHttpClient();
            }
        } else {

            if (httpClient!=null) return httpClient;

            if (Preconditions.isNotBlank(httpParam)) {

                int timeOut = httpParam.getTimeOut();
                int proxySize = httpParam.getProxyPoolSize();

                if (proxySize>1) {
                    useProxyPool = true;
                }

                Proxy proxy = httpParam.getProxy();
                BasicClientCookie cookie = httpParam.getCookie();

                if (proxy!=null) {
                    HttpHost httpHost = proxy.toHttpHost();
                    boolean check = checkProxy(httpHost);
                    if (check) { // 代理检测成功，使用代理
                        log.info("proxy："+proxy.toString()+" 代理可用");
                        httpClient = createHttpClient(timeOut,httpHost,cookie);
                    } else {
                        log.info("proxy："+proxy.toString()+" 代理不可用");
                        proxy.setFailureTimes(proxy.getFailureTimes()+1);
                        if (proxy.isDiscardProxy()) {
                            log.info("proxy："+proxy.toString()+"被丢弃");
                            httpParam.getProxyPool().remove(proxy);
                        }
                        httpClient = createHttpClient(timeOut,null,cookie);
                    }
                } else {
                    httpClient = createHttpClient(timeOut,null,cookie);
                }
            } else {
                httpClient = createHttpClient();
            }
        }

        return httpClient;
    }

    private static class Holder {
        private static final HttpManager MANAGER = new HttpManager();
    }
}
