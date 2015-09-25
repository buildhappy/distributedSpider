package com.buildhappy.test;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.javascript.ProxyAutoConfig;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.URL_MINIMAL_QUERY_ENCODING;

/**
 * Created by buildhappy on 15/9/24.
 */
public class HtmlUnitSegment {
    private static final Log LOG = LogFactory.getLog(HtmlUnitSegment.class);

    /**
     * from HttpWebConnection
     */
    protected HttpClientBuilder createHttpClient() {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            public boolean isRedirected(final HttpRequest request, final HttpResponse response,
                                        final HttpContext context) throws ProtocolException {
                return super.isRedirected(request, response, context)
                        && response.getFirstHeader("location") != null;
            }
        });
        //configureTimeout(builder, getTimeout());
        builder.setMaxConnPerRoute(6);
        return builder;
    }

    /**
     * Has the exact logic in HttpClientBuilder, but with the ability to configure
     * <code>socketFactory</code>.
     * 将HttpClientBuilder封装成到PoolingHttpClientConnectionManager中，同时支持Http和Https协议
     * from HttpWebConnection
     */
    private PoolingHttpClientConnectionManager createConnectionManager(final HttpClientBuilder builder) {
        final ConnectionSocketFactory socketFactory = new SocksConnectionSocketFactory();

        LayeredConnectionSocketFactory sslSocketFactory;
        try {
            sslSocketFactory = (LayeredConnectionSocketFactory)
                    FieldUtils.readDeclaredField(builder, "sslSocketFactory", true);
            final SocketConfig defaultSocketConfig = (SocketConfig)
                    FieldUtils.readDeclaredField(builder, "defaultSocketConfig", true);
            final ConnectionConfig defaultConnectionConfig = (ConnectionConfig)
                    FieldUtils.readDeclaredField(builder, "defaultConnectionConfig", true);
            final boolean systemProperties = (Boolean) FieldUtils.readDeclaredField(builder, "systemProperties", true);
            final int maxConnTotal = (Integer) FieldUtils.readDeclaredField(builder, "maxConnTotal", true);
            final int maxConnPerRoute = (Integer) FieldUtils.readDeclaredField(builder, "maxConnPerRoute", true);
            X509HostnameVerifier hostnameVerifier = (X509HostnameVerifier)
                    FieldUtils.readDeclaredField(builder, "hostnameVerifier", true);
            final SSLContext sslcontext = (SSLContext) FieldUtils.readDeclaredField(builder, "sslcontext", true);

            if (sslSocketFactory == null) {
                final String[] supportedProtocols = systemProperties
                        ? StringUtils.split(System.getProperty("https.protocols"), ',') : null;
                final String[] supportedCipherSuites = systemProperties
                        ? StringUtils.split(System.getProperty("https.cipherSuites"), ',') : null;
                if (hostnameVerifier == null) {
                    hostnameVerifier = SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
                }
                if (sslcontext != null) {
                    sslSocketFactory = new SSLConnectionSocketFactory(
                            sslcontext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
                }
                else {
                    if (systemProperties) {
                        sslSocketFactory = new SSLConnectionSocketFactory(
                                (SSLSocketFactory) SSLSocketFactory.getDefault(),
                                supportedProtocols, supportedCipherSuites, hostnameVerifier);
                    }
                    else {
                        sslSocketFactory = new SSLConnectionSocketFactory(
                                SSLContexts.createDefault(),
                                hostnameVerifier);
                    }
                }
            }

            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", socketFactory)
                            .register("https", sslSocketFactory)
                            .build());
            if (defaultSocketConfig != null) {
                connectionManager.setDefaultSocketConfig(defaultSocketConfig);
            }
            if (defaultConnectionConfig != null) {
                connectionManager.setDefaultConnectionConfig(defaultConnectionConfig);
            }
            if (systemProperties) {
                String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    s = System.getProperty("http.maxConnections", "5");
                    final int max = Integer.parseInt(s);
                    connectionManager.setDefaultMaxPerRoute(max);
                    connectionManager.setMaxTotal(2 * max);
                }
            }
            if (maxConnTotal > 0) {
                connectionManager.setMaxTotal(maxConnTotal);
            }
            if (maxConnPerRoute > 0) {
                connectionManager.setDefaultMaxPerRoute(maxConnPerRoute);
            }
            return connectionManager;
        }
        catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * from HttpWebConnection
     * @param request
     * @return
     * @throws IOException
     */
    public WebResponse getResponse(final WebRequest request) throws IOException {
        final HttpClientBuilder builder = null;//reconfigureHttpClientIfNeeded(getHttpClientBuilder());
        //HttpResponse httpResponse = builder.build().execute(hostConfiguration, httpMethod, httpContext_);
        return null;
    }

    /**
     * <p>Creates a page based on the specified response and inserts it into the specified window. All page
     * initialization and event notification is handled here.</p>
     *
     * Note that if the page created is an attachment page, and an {@link AttachmentHandler} has been
     * registered with this client, the page is <b>not</b> loaded into the specified window; in this case,
     * the page is loaded into a new window, and attachment handling is delegated to the registered
     * <tt>AttachmentHandler</tt>.
     *
     * @param webResponse the response that will be used to create the new page
     * @param webWindow the window that the new page will be placed within
     * @throws IOException if an IO error occurs
     * @throws FailingHttpStatusCodeException if the server returns a failing status code AND the property
     *         {@link WebClientOptions#setThrowExceptionOnFailingStatusCode(boolean)} is set to true
     * @return the newly created page
     *
     * from WebClient
     * 将WebResponse放到相应的Window中，感觉是在这个地方比较耗时
     */
    public Page loadWebResponseInto(final WebResponse webResponse, final WebWindow webWindow){
        return null;
    }


    /**
     * 该函数内实现了对页面的缓存和页面的重定向
     * 个人感觉动态网页的处理瓶就就在这个函数中，要对其进行优化
     * Loads a {@link WebResponse} from the server through the WebConnection.
     * @param webRequest the request
     * @param allowedRedirects the number of allowed redirects remaining
     * @throws IOException if an IO problem occurs
     * @return the resultant {@link WebResponse}
     * from WebClient
     */
    private WebResponse loadWebResponseFromWebConnection(final WebRequest webRequest,
                                                         final int allowedRedirects) throws IOException {
        LOG.info("WebClient: WebResponse loadWebResponseFromWebConnection(final WebRequest webRequest,final int allowedRedirects)");
        URL url = webRequest.getUrl();
        final HttpMethod method = webRequest.getHttpMethod();
        final List<NameValuePair> parameters = webRequest.getRequestParameters();

        WebAssert.notNull("url", url);
        WebAssert.notNull("method", method);
        WebAssert.notNull("parameters", parameters);

        //url = UrlUtils.encodeUrl(url, getBrowserVersion().hasFeature(URL_MINIMAL_QUERY_ENCODING),
                //webRequest.getCharset());
        webRequest.setUrl(url);

        // If the request settings don't specify a custom proxy, use the default client proxy...
        if (webRequest.getProxyHost() == null) {}

        // Add the headers that are sent with every request.
        //addDefaultHeaders(webRequest);

        // Retrieve the response, either from the cache or from the server.


        // Continue according to the HTTP status code.
        return null;
    }
}
