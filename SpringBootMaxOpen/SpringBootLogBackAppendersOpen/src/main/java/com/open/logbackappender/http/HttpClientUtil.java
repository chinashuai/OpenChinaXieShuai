package com.open.logbackappender.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * HttpClient工具类
 * 
 * @author junsun2
 * @date 2015-07-14
 */
public class HttpClientUtil {

    private boolean useHttpPool = true;
    private boolean useExpectContinue = false;

    private int connTimeout = 1000 * 5; // 默认链接超时时间
    private int soTimeout = 1000 * 60; // 默认响应超时时间

    // http代理
    private String proxyHost = null;// 自定义代理服务器的host
    private Integer proxyPort = null;// 自定义代理服务器的端口
    private String proxyAuthUserName = null;// 自定义代理服务器认证用户
    private String proxyAuthPassword = null;// 自定义代理服务器认证密码

    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private int maxPerRoute = 1000; // 每个目标主机的最大并行链接数
    private int maxTotal = 10000; // 客户端最大总并行链接数
    private String userAgent = null; // 可以设置自己的userAgent

    public void setUseHttpPool(boolean useHttpPool) {
        this.useHttpPool = useHttpPool;
    }

    public void setUseExpectContinue(boolean useExpectContinue) {
        this.useExpectContinue = useExpectContinue;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyAuthUserName(String proxyAuthUserName) {
        this.proxyAuthUserName = proxyAuthUserName;
    }

    public void setProxyAuthPassword(String proxyAuthPassword) {
        this.proxyAuthPassword = proxyAuthPassword;
    }

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final int CACHE = 512;
    private static SchemeRegistry schemeRegistry = new SchemeRegistry();

    static {
        schemeRegistry.register(new Scheme(SCHEME_HTTP, HTTP_PORT, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme(SCHEME_HTTPS, HTTPS_PORT, getSSLSocketFactory()));
    }

    private PoolingClientConnectionManager connectionManager;

    public static SSLSocketFactory getSSLSocketFactory() {
        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslcontext.init(null, new TrustManager[] { tm }, null);
            return new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        } catch (KeyManagementException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        }
    }

    private void init() {
        connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
    }

    private HttpClient buildHttpClient(int connTimeout, int soTimeout) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, soTimeout);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        if (userAgent != null) {
            HttpProtocolParams.setUserAgent(httpParams, userAgent);
        }
        HttpProtocolParams.setUseExpectContinue(httpParams, useExpectContinue);

        init();
        DefaultHttpClient client = new DefaultHttpClient(connectionManager, httpParams);
        if (StringUtils.isNotBlank(proxyHost) && (null != proxyPort)) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            if (StringUtils.isNotBlank(proxyAuthUserName)) {
                client.getCredentialsProvider().setCredentials(new AuthScope(proxyHost, proxyPort),
                        new UsernamePasswordCredentials(proxyAuthUserName, proxyAuthPassword));
            }
        }
        return client;
    }

    public static HttpClientUtil getHttpClientUtil() {
        HttpClientUtil httpclient = new HttpClientUtil();
        httpclient.init();
        return httpclient;
    }

    /**
     * 发送GET请求，默认超时时长，状态码非200时返回null
     * 
     * @param url
     * @param parameters
     * @param encoding
     * @return
     * @throws Exception
     */
    @Deprecated
    public String doGet(String url, Map<String, String> header, Map<String, String> parameters, String encoding)
            throws Exception {
        return doGet(url, header, parameters, encoding, connTimeout, soTimeout);
    }

    /**
     * 发送GET请求，自定义超时时长，状态码非200时返回null
     * 
     * @param url
     * @param parameters
     * @param encoding
     * @param connTimeout
     * @param soTimeout
     * @return
     * @throws Exception
     */
    @Deprecated
    public String doGet(String url, Map<String, String> header, Map<String, String> parameters, String encoding,
            int connTimeout, int soTimeout) throws Exception {
        HttpGet httpGet = null;
        try {
            List<Map<String, String>> parametersList = new ArrayList<Map<String, String>>();
            for (Entry<String, String> entry : parameters.entrySet()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(entry.getKey(), entry.getValue());
                parametersList.add(map);
            }
            List<NameValuePair> paramList = buildParameters(parametersList);
            if (paramList == null || paramList.isEmpty()) {
                httpGet = new HttpGet(url);
            } else {
                StringBuilder builder = new StringBuilder(url);
                builder.append(url.contains("?") ? "&" : "?");
                builder.append(URLEncodedUtils.format(paramList, "UTF-8"));
                httpGet = new HttpGet(builder.toString());
            }
            buildHeaders(httpGet, header);
            HttpResultVO httpResultVO = doRequest(httpGet, encoding, connTimeout, soTimeout);
            Integer statusCode = httpResultVO.getStatusCode();
            if (statusCode != null && 200 == httpResultVO.getStatusCode()) {
                return httpResultVO.getResponseBody();
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
    }

    /**
     * 发送POST请求，默认超时时长，状态码非200时返回null
     * 
     * @param url
     * @param parameters
     * @param encoding
     * @return
     * @throws Exception
     */
    @Deprecated
    public String doPost(String url, Map<String, String> header, Map<String, String> parameters, String encoding)
            throws Exception {
        return doPost(url, header, parameters, encoding, connTimeout, soTimeout);
    }

    /**
     * 发送POST请求，自定义超时时长，状态码非200时返回null
     * 
     * @param url
     * @param parameters
     * @param encoding
     * @param connTimeout
     * @param soTimeout
     * @return
     * @throws SocketTimeoutException
     * @throws ConnectTimeoutException
     * @throws Exception
     */
    @Deprecated
    public String doPost(String url, Map<String, String> header, Map<String, String> parameters, String encoding,
            int connTimeout, int soTimeout) throws Exception {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            List<Map<String, String>> parametersList = new ArrayList<Map<String, String>>();
            for (Entry<String, String> entry : parameters.entrySet()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(entry.getKey(), entry.getValue());
                parametersList.add(map);
            }
            List<NameValuePair> paramList = buildParameters(parametersList);
            if (paramList != null && !paramList.isEmpty()) {
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            }
            buildHeaders(httpPost, header);
            HttpResultVO httpResultVO = doRequest(httpPost, encoding, connTimeout, soTimeout);
            Integer statusCode = httpResultVO.getStatusCode();
            if (statusCode != null && 200 == httpResultVO.getStatusCode()) {
                return httpResultVO.getResponseBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    /**
     * 发送GET请求，默认超时时长
     * 
     * @param url
     * @param header
     * @param parameters
     * @param encoding
     * @return
     * @throws Exception
     */
    public HttpResultVO doGetDetail(String url, Map<String, String> header, Map<String, String> parameters,
            String encoding) throws Exception {
        List<Map<String, String>> parametersList = new ArrayList<Map<String, String>>();
        for (Entry<String, String> entry : parameters.entrySet()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(entry.getKey(), entry.getValue());
            parametersList.add(map);
        }
        return doGetDetail(url, header, parametersList, encoding, connTimeout, soTimeout);
    }

    /**
     * 发送GET请求，默认超时时长
     * 
     * @param url
     * @param header
     * @param parameters
     * @param encoding
     * @return
     * @throws Exception
     */
    public HttpResultVO doGetDetail(String url, Map<String, String> header, List<Map<String, String>> parameters,
            String encoding) throws Exception {
        return doGetDetail(url, header, parameters, encoding, connTimeout, soTimeout);
    }

    /**
     * 发送GET请求，自定义超时时长
     * 
     * @param url
     * @param header
     * @param parameters
     * @param encoding
     * @param connTimeout
     * @param soTimeout
     * @return
     * @throws Exception
     */
    public HttpResultVO doGetDetail(String url, Map<String, String> header, List<Map<String, String>> parameters,
            String encoding, int connTimeout, int soTimeout) throws Exception {
        HttpGet httpGet = null;
        try {
            List<NameValuePair> paramList = buildParameters(parameters);
            if (paramList == null || paramList.isEmpty()) {
                httpGet = new HttpGet(url);
            } else {
                StringBuilder builder = new StringBuilder(url);
                builder.append(url.contains("?") ? "&" : "?");
                builder.append(URLEncodedUtils.format(paramList, "UTF-8"));
                httpGet = new HttpGet(builder.toString());
            }
            buildHeaders(httpGet, header);
            HttpResultVO httpResultVO = doRequest(httpGet, encoding, connTimeout, soTimeout);
            return httpResultVO;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
    }

    /**
     * 发送POST请求，默认超时时长
     * 
     * @param url
     * @param header
     * @param parameters
     * @param encoding
     * @return
     * @throws Exception
     */
    public HttpResultVO doPostDetail(String url, Map<String, String> header, List<Map<String, String>> parameters,
            String encoding) throws Exception {
        return doPostDetail(url, header, parameters, encoding, connTimeout, soTimeout);
    }

    /**
     * 发送POST请求，默认超时时长
     * 
     * @param url
     * @param header
     * @param parameters
     * @param encoding
     * @return
     * @throws Exception
     */
    public HttpResultVO doPostDetail(String url, Map<String, String> header, Map<String, String> parameters,
            String encoding) throws Exception {
        List<Map<String, String>> parametersList = new ArrayList<Map<String, String>>();
        if (parameters != null && parameters.size() > 0) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(entry.getKey(), entry.getValue());
                parametersList.add(map);
            }
        }
        return doPostDetail(url, header, parametersList, encoding, connTimeout, soTimeout);
    }

    public HttpResultVO doPostWithFile(String url, Map<String, String> header, Map<String, String> parameters,
            Map<String, File> fileMap, String encoding) throws Exception {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            MultipartEntity entity = new MultipartEntity();

            for (Entry<String, String> entry : parameters.entrySet()) {
                entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
            }
            for (Entry<String, File> entry : fileMap.entrySet()) {
                entity.addPart(entry.getKey(), new FileBody(entry.getValue()));
            }
            httpPost.setEntity(entity);
            buildHeaders(httpPost, header);
            HttpResultVO httpResultVO = doRequest(httpPost, encoding, connTimeout, soTimeout);
            return httpResultVO;
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    /**
     * 发送POST请求，自定义超时时长
     * 
     * @param url
     * @param header
     * @param parameters
     * @param encoding
     * @param connTimeout
     * @param soTimeout
     * @return
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     * @throws Exception
     */
    public HttpResultVO doPostDetail(String url, Map<String, String> header, List<Map<String, String>> parameters,
            String encoding, int connTimeout, int soTimeout) throws Exception {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            List<NameValuePair> paramList = buildParameters(parameters);
            if (paramList != null && !paramList.isEmpty()) {
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            }
            buildHeaders(httpPost, header);
            HttpResultVO httpResultVO = doRequest(httpPost, encoding, connTimeout, soTimeout);
            return httpResultVO;
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    /**
     * 发送POST请求，正文内容为bodyString，自定义超时时长
     * 
     * @param url
     * @param header
     * @param body
     * @param encoding
     * @param connTimeout
     * @param soTimeout
     * @return
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     * @throws Exception
     */
    public HttpResultVO doPostStringDetail(String url, Map<String, String> header, String body, String encoding,
            int connTimeout, int soTimeout) throws Exception {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            if (body != null && !"".equals(body)) {
                httpPost.setEntity(new ByteArrayEntity(body.getBytes(encoding)));
            }
            buildHeaders(httpPost, header);
            HttpResultVO httpResultVO = doRequest(httpPost, encoding, connTimeout, soTimeout);
            return httpResultVO;
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    private void buildHeaders(HttpUriRequest request, Map<String, String> header) {
        if (header != null && header.size() > 0) {
            Set<String> keys = header.keySet();
            for (String key : keys) {
                String value = header.get(key);
                request.addHeader(key, value);
            }
        }
    }

    /**
     * 组装请求参数
     */
    private List<NameValuePair> buildParameters(List<Map<String, String>> parameters) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        for (Map<String, String> pair : parameters) {
            Set<Entry<String, String>> entry = pair.entrySet();
            for (Entry<String, String> e : entry) {
                NameValuePair p = new BasicNameValuePair(e.getKey(), e.getValue());
                paramList.add(p);
            }

        }
        return paramList;
    }

    /**
     * 发送HTTP请求,是否200都正常返回，如果发生异常则抛出异常
     * 
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     */
    private HttpResultVO doRequest(HttpUriRequest request, String encoding, int connTimeout, int soTimeout)
            throws ConnectTimeoutException, SocketTimeoutException {
        HttpResultVO httpResultVO = new HttpResultVO();
        StringBuilder builder = new StringBuilder();
        InputStream in = null;
        InputStreamReader reader = null;
        HttpClient httpClient = this.buildHttpClient(connTimeout, soTimeout);
        HttpResponse httpResponse = null;
        long startTime = System.currentTimeMillis();
        try {
            httpResultVO.setUrl(request.getURI().toURL().toString());
            if (!this.useHttpPool) {
                request.setHeader("Connection", "close");
            }
            httpResponse = httpClient.execute(request);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            httpResultVO.setStatusCode(statusCode);
            Header[] heads = httpResponse.getAllHeaders();
            if (heads != null) {
                for (Header head : heads) {
                    httpResultVO.getResponseHead().put(head.getName(), head.getValue());
                }
            }
            HttpEntity httpEntity = httpResponse.getEntity();
            in = httpEntity.getContent();
            reader = new InputStreamReader(in, encoding);
            char[] buffer = new char[CACHE];
            int len = 0;
            while ((len = reader.read(buffer, 0, buffer.length)) != -1) {
                builder.append(buffer, 0, len);
            }
            httpResultVO.setResponseBody(builder.toString());
            return httpResultVO;
        } catch (ConnectTimeoutException cte) {
            throw cte;
        } catch (SocketTimeoutException ste) {
            throw ste;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
                if (!this.useHttpPool) {
                    httpClient.getConnectionManager().shutdown();
                }
                httpResultVO.setResponsetime(System.currentTimeMillis() - startTime);
            } catch (IOException e) {
            }

        }

    }
}