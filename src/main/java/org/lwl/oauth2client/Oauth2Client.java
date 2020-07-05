package org.lwl.oauth2client;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.lwl.oauth2client.config.Oauth2Properties;
import org.lwl.oauth2client.dto.Message;
import org.lwl.oauth2client.dto.Token;
import org.lwl.oauth2client.utils.HttpClientUtil;

import java.util.*;

/**
 * Oauth2Client
 *
 * @author Lwl
 * @version 1.0
 */
@Slf4j
public class Oauth2Client {

    private static Oauth2Client client = new Oauth2Client();
    private static Oauth2Properties prop;
    /**
     * 取得Oauth2客户端实例
     * @param prop 客户端属性
     *
     * @return Oauth2客户端实例
     */
    public static Oauth2Client getInstance(Oauth2Properties prop) {

        Oauth2Client.prop = prop;
        return client;
    }

    public Oauth2Properties getProp() {
        return prop;
    }

    /**
     * 调用统一认证服务器/oauth/authorize接口，同时对登录用户名和登录密码进行认证，
     * 认证后回调用户设置的redirectUri，返回code和state
     *
     * @param state    状态（回调时原值返回 非必须项）
     * @param userName 登录用户名
     * @param password 登录密码
     * @return 调用结果消息
     */
    public Message authorize(String state, String userName, String password) {
        CloseableHttpClient httpclient = HttpClientUtil.createHttpClient(prop.getHttps());
        List<Header> headers = new ArrayList<Header>() {
            {
                add(new BasicHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes())));

            }
        };
        return authorize(httpclient, state, headers);
    }

    /**
     * 调用统一认证服务器/oauth/authorize接口，弹出登录对话框，
     * 认证后回调用户设置的redirectUri，返回code和state
     *
     * @param state 状态（回调时原值返回 非必须项）
     * @return URL地址
     */
    public String authorizeUrl(String state) {
        return authorizeUrl(state, null);
    }

    /**
     * 调用统一认证服务器/oauth/authorize接口，弹出登录对话框，
     * 认证后回调用户设置的redirectUri，返回code和state
     * 可以通过传入color参数设置登录按钮颜色
     * 目前支持颜色：blue,green,gray,red
     *
     * @param state 状态（回调时原值返回 非必须项）
     * @param color 按钮颜色（非必须项）
     * @return URL地址
     */
    public String authorizeUrl(String state, String color) {
        String url = prop.getServerIp()
                + "/oauth/authorize?"
                + "response_type=code"
                + "&scope=" + prop.getScope()
                + "&client_id=" + prop.getClientId()
                + "&redirect_uri=" + prop.getRedirectUri();
        if (state != null && !"".equals(state.trim())) {
            url = url + "&state=" + state;
        }
        if (color != null && !"".equals(color.trim())) {
            url = url + "&color=" + color;
        }
        return url;
    }

    /**
     * 调用统一认证服务器/oauth/token接口取得令牌，传入用户回调接口取得的code值。
     *
     * @param code 用户回调接口取得的code值
     * @return 调用结果消息
     */
    public Message getToken(String code) {
        CloseableHttpClient httpclient = HttpClientUtil.createHttpClient(prop.getHttps());
        String url = prop.getServerIp() + "/oauth/token";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("code", code);
        paramMap.put("redirect_uri", prop.getRedirectUri());
        this.addPostCommonParams(paramMap);
        return HttpClientUtil.postCommon(url, httpclient, paramMap);
    }

    /**
     * 调用统一认证服务器/oauth/token接口取得令牌，传入用户回调接口取得的code值。
     *
     * @param code
     * @return
     */
    public Token getTokenbyCode(String code) {
        Message message = getToken(code);
        if (message != null && message.getStatusCode() == HttpStatus.SC_OK) {
            return JSONObject.parseObject(message.getContent(), Token.class);
        }
        return null;
    }

    /**
     * 调用统一认证服务器/oauth/check_token校验令牌，认证服务器限制此功能时不可用。
     *
     * @param token 令牌
     * @return 调用结果消息
     */
    public Message checkToken(String token) {
        CloseableHttpClient httpclient = HttpClientUtil.createHttpClient(prop.getHttps());
        String url = prop.getServerIp() + "/oauth/check_token";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("token", token);
        this.addPostCommonParams(paramMap);
        return HttpClientUtil.postCommon(url, httpclient, paramMap);
    }

    /**
     * 调用统一认证服务器/oauth/refresh_token刷新令牌，认证服务器限制此功能时不可用。
     *
     * @param refreshToken 刷新令牌
     * @return 调用结果消息
     */
    public Message refreshToken(String refreshToken) {
        CloseableHttpClient httpclient = HttpClientUtil.createHttpClient(prop.getHttps());
        String url = prop.getServerIp() + "/oauth/token";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("grant_type", "refresh_token");
        paramMap.put("refresh_token", refreshToken);
        this.addPostCommonParams(paramMap);
        return HttpClientUtil.postCommon(url, httpclient, paramMap);
    }

    /**
     * 通过令牌调用统一认证服务器相应接口，取得用户信息等资源。
     *
     * @param token     令牌
     * @param targetUrl 服务器接口URL
     * @return 调用结果消息
     */
    public Message executeOauthQuery(String token, String targetUrl, Map<String, String> paramMap) {
        CloseableHttpClient httpclient = HttpClientUtil.createHttpClient(prop.getHttps());
        String url = prop.getServerIp() + targetUrl;
        List<Header> headers = new ArrayList<Header>() {
            {
                add(new BasicHeader("Authorization", "Bearer " + token));
            }
        };
        return HttpClientUtil.postCommon(url, httpclient, paramMap, headers);
    }

    /**
     * 通过令牌调用统一认证服务器，取得用户信息。
     *
     * @param token 令牌
     * @return 调用结果消息
     */
    public Message getUserInfo(String token) {
        return executeOauthQuery(token, "/me", null);
    }


    private Message authorize(CloseableHttpClient httpclient, String state, List<Header> headers) {
        String url = authorizeUrl(state);
        return HttpClientUtil.getCommon(url, httpclient, headers);
    }
    /**
     * 取得认证服务器登出URL
     *
     * @return URL地址
     */
    public String exitUrl() {
        return prop.getServerIp() + "/oauth/exit";
    }

    private void addPostCommonParams(Map<String, String> paramMap) {
        paramMap.put("client_id", prop.getClientId());
        paramMap.put("timestamp", createTimestamp());
    }

    private String createTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
}
