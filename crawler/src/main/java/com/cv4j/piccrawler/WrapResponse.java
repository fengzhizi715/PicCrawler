package com.cv4j.piccrawler;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.InputStream;

/**
 * Created by tony on 2017/10/11.
 */
public class WrapResponse {

    public CloseableHttpResponse response;
    public InputStream is;

    public WrapResponse(CloseableHttpResponse response,InputStream is) {

        this.response = response;
        this.is = is;
    }
}
