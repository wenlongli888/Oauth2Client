package org.lwl.oauth2client.utils;

import org.lwl.oauth2client.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpClientUtil {

    public static UrlEncodedFormEntity createFormEntity(Map<String, String> paramMap){
        List<NameValuePair> list = new LinkedList<>();
        UrlEncodedFormEntity entity = null;
        try {
            if(paramMap != null){
                paramMap.forEach((k,v)->list.add(new BasicNameValuePair(k, v)));
            }
            entity = new UrlEncodedFormEntity(list,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return entity;
    }
    public static CloseableHttpClient createHttpClient(Boolean https) {
        if (Boolean.TRUE.equals(https)) {
            try {
                //指定信任密钥存储对象和连接套接字工厂
                SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (a, b) -> true).build();
                return HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext,
                        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return HttpClients.createDefault();
    }
    public static CloseableHttpClient createCloseableHttpClientWithBasicAuth(String userName, String password) {
        // create HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // setting BasicAuth
        CredentialsProvider provider = new BasicCredentialsProvider();
        // Create the authentication scope
        AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
        // Create credential pair，在此处填写用户名和密码createCloseableHttpClientWithBasicAuth
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        // Inject the credentials
        provider.setCredentials(scope, credentials);
        // Set the default credentials provider
        httpClientBuilder.setDefaultCredentialsProvider(provider);
        // HttpClient
        return httpClientBuilder.build();
    }
    public static Message postCommon(String url, CloseableHttpClient httpclient) {
        return postCommon(url, httpclient, null, null);
    }
    public static Message postCommon(String url, CloseableHttpClient httpclient, Object paramObj){
        return postCommon(url, httpclient, ConvertUtil.Obj2Map(paramObj), null);
    }
    public static Message postCommon(String url, CloseableHttpClient httpclient, Map<String, String> paramMap){
        return postCommon(url, httpclient, paramMap, null);
    }
    public static Message postCommon(String url, CloseableHttpClient httpclient, Map<String, String> paramMap, List<Header> headers) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            if (paramMap != null) {
                httpPost.setEntity(HttpClientUtil.createFormEntity(paramMap));
            }
            if(headers != null){
                headers.forEach(httpPost::addHeader);
            }
            response = httpclient.execute(httpPost);
            Message message = new Message();
            int statusCode = response.getStatusLine().getStatusCode();//获取返回的状态值
            message.setStatusCode(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                message.setContent(result);
            }

            log.debug("postCommon statusCode:" + statusCode);
            return message;
        } catch (Exception e) {
            log.error("HttpPost Exception handle-- > " + e);
        } finally {
            if (response != null) {
                try {
                    response.close();//关闭response
                } catch (IOException e) {
                    log.error("HttpPost IOException handle--> " + e);
                }
            }
            if (httpclient != null) {
                try {
                    httpclient.close();//关闭httpclient
                } catch (IOException e) {
                    log.error("HttpPost IOException handle--> " + e);
                }
            }
        }
        return null;
    }
    public static Message getCommon(String url, CloseableHttpClient httpclient){
        return getCommon(url,httpclient);
    }
    public static Message getCommon(String url, CloseableHttpClient httpclient, List<Header> headers){
        // execute get request
        CloseableHttpResponse response = null;
        Message message = new Message();
        try {
            HttpGet httpget = new HttpGet(url);
            if(headers != null){
                headers.forEach(httpget::addHeader);
            }
            response = httpclient.execute(httpget);
            int statusCode = response.getStatusLine().getStatusCode();//获取返回的状态值
            message.setStatusCode(statusCode);
            if (statusCode == HttpStatus.SC_OK) {

            } else {
                log.warn("authorize statusCode:" + statusCode);
            }
            return message;

        } catch (Exception e) {
            log.error("httpGet Exception handle-- > " + e);
        } finally {
            if (response != null) {
                try {
                    response.close();//关闭response
                } catch (IOException e) {
                    log.error("httpGet IOException handle--> " + e);
                }
            }
            if (httpclient != null) {
                try {
                    httpclient.close();//关闭httpclient
                } catch (IOException e) {
                    log.error("httpGet IOException handle--> " + e);
                }
            }
        }
        return null;

    }
}
