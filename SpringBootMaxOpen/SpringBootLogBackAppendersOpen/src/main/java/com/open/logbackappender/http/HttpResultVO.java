package com.open.logbackappender.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResultVO {

    /**
     * http请求的url
     */
    private String url = null;
    /**
     * http请求返回响应状态码
     */
    private Integer statusCode = null;
    /**
     * http请求返回响应头信息
     */
    private Map<String, String> responseHead = new HashMap<String, String>();
    /**
     * http请求返回响应正文内容
     */
    private String responseBody = null;

    /**
     * http请求返回响应时间
     */
    private Long responsetime = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getResponseHead() {
        return responseHead;
    }

    public void setResponseHead(Map<String, String> responseHead) {
        this.responseHead = responseHead;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Long getResponsetime() {
        return responsetime;
    }

    public void setResponsetime(Long responsetime) {
        this.responsetime = responsetime;
    }

}
