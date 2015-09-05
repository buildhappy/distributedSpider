package com.buildhappy.downloaderModel.downloader;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import com.buildhappy.downloaderModel.bean.Site;

import java.io.IOException;
import java.util.Map;

/**
 * @author code4crafter@gmail.com <br>
 * @since 0.4.0
 * 该类的作用是利用Site信息，创建链接，产生cookie
 * 相关类的手册：
 * http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/index.html?org/apache/http/impl/conn/PoolingClientConnectionManager.html
 */
public class HttpClientGenerator {

	/**
	 * PoolingHttpClientConnectionManager:管理OperatedClientConnection链接池，
	 * 当一个route已经建立了链接时，处理该route的其他请求时，将不会再创建链接而是从该连接池中取出已有的链接为其服务。
	 * 每个route最多管理20个链接，并且链接的并发处理数为2
	 * Manages a pool of OperatedClientConnection and is able to service connection requests from multiple execution threads. 
	 * Connections are pooled on a per route basis. 
	 * A request for a route which already the manager has persistent connections for available in the pool 
	 * will be services by leasing a connection from the pool rather than creating a brand new connection.
	 * PoolingConnectionManager maintains a maximum limit of connection on a per route basis and in total. 
	 * Per default this implementation will create no more than than 2 concurrent connections per given route 
	 * and no more 20 connections in total. 
	 * For many real-world applications these limits may prove too constraining, 
	 * especially if they use HTTP as a transport protocol for their services. 
	 * Connection limits, however, can be adjusted using HTTP parameters.
	 */
    private PoolingHttpClientConnectionManager connectionManager;

    public HttpClientGenerator() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        connectionManager = new PoolingHttpClientConnectionManager(reg);
        connectionManager.setDefaultMaxPerRoute(100);
    }

    public HttpClientGenerator setPoolSize(int poolSize) {
        connectionManager.setMaxTotal(poolSize);
        return this;
    }

    public CloseableHttpClient getClient(Site site) {
        return generateClient(site);
    }

    private CloseableHttpClient generateClient(Site site) {
    	//HttpClientBuilder创建ClosableHttpClientBuilder(Base implementation of HttpClient that also implements Closeable.)
        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
        if (site != null && site.getUserAgent() != null) {
            httpClientBuilder.setUserAgent(site.getUserAgent());
        } else {
            httpClientBuilder.setUserAgent("");
        }
        /*
         * Http请求拦截器，产生或者拦截具有特殊类型Header的Http请求。
         * HttpRequestInterceptor:is a routine that implements a specific aspect of the HTTP protocol. 
         * Usually protocol interceptors are expected to act upon one specific header or 
         * a group of related headers of the incoming message or populate the outgoing message 
         * with one specific header or a group of related headers.
         */
        if (site == null || site.isUseGzip()) {
            httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {

                public void process(
                        final HttpRequest request,
                        final HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }

                }
            });
        }
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();
        httpClientBuilder.setDefaultSocketConfig(socketConfig);
        if (site != null) {
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(site.getRetryTimes(), true));
        }
        generateCookie(httpClientBuilder, site);
        return httpClientBuilder.build();
    }

    /**
     * 根据Site中的cookie信息，来构造cookie
     * @param httpClientBuilder
     * @param site
     */
    private void generateCookie(HttpClientBuilder httpClientBuilder, Site site) {
        CookieStore cookieStore = new BasicCookieStore();
        for (Map.Entry<String, String> cookieEntry : site.getCookies().entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(site.getDomain());
            cookieStore.addCookie(cookie);
        }
        for (Map.Entry<String, Map<String, String>> domainEntry : site.getAllCookies().entrySet()) {
            for (Map.Entry<String, String> cookieEntry : domainEntry.getValue().entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
                cookie.setDomain(domainEntry.getKey());
                cookieStore.addCookie(cookie);
            }
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);
    }

}
