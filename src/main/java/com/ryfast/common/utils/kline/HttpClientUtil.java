package com.ryfast.common.utils.kline;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {
    public static final Logger LOGGER = LogManager.getLogger(HttpClientUtil.class);
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 发送简单GET请求
     *
     * @param url 请求url
     * @return
     * @throws IOException
     */
    public static String doGet(String url) throws IOException {
        return doGet(url, null, null);
    }

    /**
     * 发送复杂GET请求
     *
     * @param url     请求url
     * @param params  请求参数信息
     * @param headers 请求头信息
     * @return
     * @throws IOException
     */
    public static String doGet(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        CloseableHttpClient closeableHttpClient = customHttpClient();
        HttpGet httpGet = new HttpGet(url + buildGetParams(params));
        // 设置请求头
        buildHeaders(headers, httpGet);

        CloseableHttpResponse response = null;
        String retStr = null;
        try {
            response = closeableHttpClient.execute(httpGet);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    retStr = EntityUtils.toString(entity, CHARSET_UTF8);
                    EntityUtils.consume(entity);
                }
            } finally {
                response.close();
            }
        } finally {
            closeableHttpClient.close();
        }
        return retStr;
    }

    /**
     * 将请求头设置到request中
     *
     * @param headers
     * @param httpRequest
     */
    private static void buildHeaders(Map<String, String> headers, HttpRequest httpRequest) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpRequest.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * 将参数map转成相应的参数字符串
     *
     * @param params
     * @return
     */
    private static String buildGetParams(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                paramStr.append("&").append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), CHARSET_UTF8));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("UnsupportedEncoding", e);
                return null;
            }
        }
        if (paramStr.length() > 0) {
            paramStr.replace(0, 1, "?");
        }
        return paramStr.toString();
    }

    /**
     * 发送简单POST请求
     *
     * @param url 请求url
     * @return
     * @throws IOException
     */
    public static String doPost(String url) throws IOException {
        return post(url, null, null);
    }

    /**
     * 发送POST请求（contentType: application/json）
     *
     * @param url        请求url
     * @param jsonParams 请求json字符串
     * @return
     * @throws IOException
     */
    public static String doPost(String url, String jsonParams) throws IOException {
        return post(url, buildHttpEntity(jsonParams), null);
    }

    /**
     * 发送POST请求（contentType: application/x-www-form-urlencoded）
     *
     * @param url    请求url
     * @param params 请求参数信息
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> params) throws IOException {
        return post(url, buildHttpEntity(params), null);
    }

    /**
     * 发送带自定义请求头的POST请求（contentType: application/json）
     *
     * @param url        请求url
     * @param jsonParams 请求json字符串
     * @param headers    请求头信息
     * @return
     * @throws IOException
     */
    public static String doPost(String url, String jsonParams, Map<String, String> headers) throws IOException {
        return post(url, buildHttpEntity(jsonParams), headers);
    }

    /**
     * 发送带自定义请求头的POST请求（contentType: application/x-www-form-urlencoded）
     *
     * @param url     请求url
     * @param params  请求参数信息
     * @param headers 请求头信息
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return post(url, buildHttpEntity(params), headers);
    }

    /**
     * POST请求实际执行方法
     *
     * @param url
     * @param httpEntity
     * @param headers
     * @return
     * @throws IOException
     */
    private static String post(String url, HttpEntity httpEntity, Map<String, String> headers) throws IOException {
        CloseableHttpClient closeableHttpClient = customHttpClient();
        HttpPost httpPost = new HttpPost(url);
        if (httpEntity != null) {
            httpPost.setEntity(httpEntity);
        }

        // 设置请求头
        buildHeaders(headers, httpPost);

        CloseableHttpResponse response = null;
        String retStr = null;
        try {
            response = closeableHttpClient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    retStr = EntityUtils.toString(entity, CHARSET_UTF8);
                    EntityUtils.consume(entity);
                }
            } finally {
                response.close();
            }
        } finally {
            closeableHttpClient.close();
        }
        return retStr;
    }

    /**
     * 创建HttpEntity（contentType: application/json）
     *
     * @param jsonParams
     * @return
     */
    private static HttpEntity buildHttpEntity(String jsonParams) {
        // 构建消息实体 发送Json格式的数据
        StringEntity entity = new StringEntity(jsonParams, ContentType.APPLICATION_JSON);
        entity.setContentEncoding(CHARSET_UTF8);

        return entity;
    }

    /**
     * 创建HttpEntity（contentType: application/x-www-form-urlencoded）
     *
     * @param params
     * @return
     */
    private static HttpEntity buildHttpEntity(Map<String, String> params) {
        HttpEntity entity = null;
        List<NameValuePair> paramsList = new ArrayList<>(params.size());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        try {
            entity = new UrlEncodedFormEntity(paramsList, CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("getHttpEntity() error:", e);
        }
        return entity;
    }

    /**
     * 自定义一个HttpClient
     *
     * @return
     */
    private static CloseableHttpClient customHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        // 绕过不安全的https请求的证书验证
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", buildSSLConnectionSocketFactory()).build();
        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager(registry);
        // 使用默认值
        poolingConnManager.setMaxTotal(5 * 2);
        // 一个域名对应一个路由
        poolingConnManager.setDefaultMaxPerRoute(5);
        httpClientBuilder.setConnectionManager(poolingConnManager);

        // 设置超时配置
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000).build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        // 设置请求头
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN"));
        headers.add(new BasicHeader("Connection", "Keep-Alive"));
        httpClientBuilder.setDefaultHeaders(headers);

        return httpClientBuilder.build();
    }

    /**
     * 创建支持安全协议的ConnectionSocketFactory
     *
     * @return ConnectionSocketFactory
     */
    private static ConnectionSocketFactory buildSSLConnectionSocketFactory() {
        SSLContext sslContext = null;
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
                // 判断是否信任url
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslContext = sslContextBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 支持的安全协议
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null,
                NoopHostnameVerifier.INSTANCE);

        return sslConnectionSocketFactory;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(HttpClientUtil.doGet("https://inv-veri.chinatax.gov.cn/"));
    }
}
