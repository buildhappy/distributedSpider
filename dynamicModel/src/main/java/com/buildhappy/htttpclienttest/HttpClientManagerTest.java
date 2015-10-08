package com.buildhappy.htttpclienttest;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by buildhappy on 15/9/24.
 */
public class HttpClientManagerTest {
    /**
     * Case 1
     * HttpClient使用一个叫做Http连接管理器的特殊实体类来管理Http连接，这个实体类要实现HttpClientConnectionManager接口。
     * Http连接管理器在新建http连接时，作为工厂类；管理持久http连接的生命周期；同步持久连接(确保线程安全，即一个http连接同一时间只能被一个线程访问)。
     * Http连接管理器和ManagedHttpClientConnection的实例类一起发挥作用，
     * ManagedHttpClientConnection实体类可以看做http连接的一个代理服务器，管理着I/O操作。
     * 如果一个Http连接被释放或者被它的消费者明确表示要关闭，那么底层的连接就会和它的代理进行分离，并且该连接会被交还给连接管理器。
     * 这是，即使服务消费者仍然持有代理的引用，它也不能再执行I/O操作，或者更改Http连接的状态
     * <p>
     * 下面展示了使用BasicHttpClientConnectionManager管理连接
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws ConnectionPoolTimeoutException
     */
    public static void basicHttpClientConnectionManager() throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        HttpClientContext context = HttpClientContext.create();
        //BasicHttpClientConnectionManager是个简单的连接管理器，它一次只能管理一个连接。
        //尽管这个类是线程安全的，它在同一时间也只能被一个线程使用。
        //BasicHttpClientConnectionManager会尽量重用旧的连接来发送后续的请求，并且使用相同的路由。
        //如果后续请求的路由和旧连接中的路由不匹配，BasicHttpClientConnectionManager就会关闭当前连接，使用请求中的路由重新建立连接。
        //如果当前的连接正在被占用，会抛出java.lang.IllegalStateException异常。
        //相对BasicHttpClientConnectionManager来说，PoolingHttpClientConnectionManager是个更复杂的类
        HttpClientConnectionManager connMrg = new BasicHttpClientConnectionManager();
        HttpRoute route = new HttpRoute(new HttpHost("www.yeetrack.com", 80));
        // 获取新的连接. 这里可能耗费很多时间
        ConnectionRequest connRequest = connMrg.requestConnection(route, null);
        // 10秒超时
        HttpClientConnection conn = connRequest.get(10, TimeUnit.SECONDS);
        try {
            // 如果创建连接失败
            if (!conn.isOpen()) {
                // establish connection based on its route info
                connMrg.connect(conn, route, 1000, context);
                // and mark it as route complete
                connMrg.routeComplete(conn, route, context);
            }
            // 进行自己的操作.
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connMrg.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * Case 2:下面展示了PoolingHttpClientConnectionManager参数设置 <p>
     */
    public static void poolingHttpClientConnectionManager() throws IOException {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //将最大连接数增加到200
        cm.setMaxTotal(200);
        //将每个路由基础的连接增加到20
        cm.setDefaultMaxPerRoute(20);
        //将目标主机的最大连接数增加到50
        HttpHost localhost = new HttpHost("www.yeetrack.com", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 50);

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        //释放连接，当一个HttpClient的实例不在使用，或者已经脱离它的作用范围，
        //我们需要关掉它的连接管理器，来关闭掉所有的连接，释放掉这些连接占用的系统资源。
        httpClient.close();
    }

    /**
     * Case 3:多线程请求执行请求连接
     * 当使用了请求连接池管理器(比如PoolingClientConnectionManager)后，HttpClient就可以同时执行多个线程的请求了。<p>
     * PoolingClientConnectionManager会根据它的配置来分配请求连接。<p>
     * 如果连接池中的所有连接都被占用了，那么后续的请求就会被阻塞，直到有连接被释放回连接池中。<p>
     * 为了防止永远阻塞的情况发生，我们可以把http.conn-manager.timeout的值设置成一个整数。<p>
     * 如果在超时时间内，没有可用连接，就会抛出ConnectionPoolTimeoutException异常。<p>
     */
    public static void mutiThreadGetConnection() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //将最大连接数增加到200
        cm.setMaxTotal(200);
        //将每个路由基础的连接增加到20
        cm.setDefaultMaxPerRoute(20);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        // URL列表数组
        String[] urisToGet = {
                "http://www.domain1.com/",
                "http://www.domain2.com/",
                "http://www.domain3.com/",
                "http://www.domain4.com/"
        };

        // 为每个url创建一个线程，GetThread是自定义的类
        GetThread[] threads = new GetThread[urisToGet.length];
        for (int i = 0; i < threads.length; i++) {
            HttpGet httpget = new HttpGet(urisToGet[i]);
            threads[i] = new GetThread(httpClient, httpget);
        }

        // 启动线程
        for (int j = 0; j < threads.length; j++) {
            threads[j].start();
        }

        // join the threads
        for (int j = 0; j < threads.length; j++) {
            try {
                threads[j].join();//Waits for this thread to die.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static class GetThread extends Thread {

        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;

        public GetThread(CloseableHttpClient httpClient, HttpGet httpget) {
            this.httpClient = httpClient;
            this.context = HttpClientContext.create();
            this.httpget = httpget;
        }
        @Override
        public void run() {
            try {
                CloseableHttpResponse response = httpClient.execute(
                        httpget, context);
                try {
                    HttpEntity entity = response.getEntity();
                } finally {
                    response.close();
                }
            } catch (ClientProtocolException ex) {
                // Handle protocol errors
            } catch (IOException ex) {
                // Handle I/O errors
            }
        }

    }

    /**
     * Case 4:连接的释放
     * 经典阻塞I/O模型的一个主要缺点就是只有当组侧I/O时，socket才能对I/O事件做出反应。
     * 当连接被管理器收回后，这个连接仍然存活，但是却无法监控socket的状态，也无法对I/O事件做出反馈。
     * 如果连接被服务器端关闭了，客户端监测不到连接的状态变化（也就无法根据连接状态的变化，关闭本地的socket）。
     * HttpClient为了缓解这一问题造成的影响，会在使用某个连接前，监测这个连接是否已经过时，如果服务器端关闭了连接，那么连接就会失效。
     * 这种过时检查并不是100%有效，并且会给每个请求增加10到30毫秒额外开销。
     * 唯一一个可行的，且does not involve a one thread per socket model for idle connections的解决办法，
     * 即建立一个监控线程，来专门回收由于长时间不活动而被判定为失效的连接。
     * 这个监控线程可以周期性的调用ClientConnectionManager类的closeExpiredConnections()方法来关闭过期的连接，回收连接池中被关闭的连接。
     它也可以选择性的调用ClientConnectionManager类的closeIdleConnections()方法来关闭一段时间内不活动的连接。
     */
    public static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // 关闭失效的连接
                        connMgr.closeExpiredConnections();
                        // 可选的, 关闭30秒内不活动的连接
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
    /**
     * Case 5:连接的存活策略
     * Http规范没有规定一个持久连接应该保持存活多久。
     * 有些Http服务器使用非标准的Keep-Alive头消息和客户端进行交互，服务器端会保持数秒时间内保持连接。
     * HttpClient也会利用这个头消息。如果服务器返回的响应中没有包含Keep-Alive头消息，HttpClient会认为这个连接可以永远保持。
     * 然而，很多服务器都会在不通知客户端的情况下，关闭一定时间内不活动的连接，来节省服务器资源。
     * 在某些情况下默认的策略显得太乐观，我们可能需要自定义连接存活策略。
     */
    public static void keepAlivedStrategy(){
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch(NumberFormatException ignore) {
                        }
                    }
                }
                HttpHost target = (HttpHost) context.getAttribute(
                        HttpClientContext.HTTP_TARGET_HOST);
                if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
                    // Keep alive for 5 seconds only
                    return 5 * 1000;
                } else {
                    // otherwise keep alive for 30 seconds
                    return 30 * 1000;
                }
            }

        };
        CloseableHttpClient client = HttpClients.custom()
                .setKeepAliveStrategy(myStrategy)
                .build();
    }

    /**
     * Case 6:socket连接的管理
     * Http连接使用java.net.Socket类来传输数据。这依赖于ConnectionSocketFactory接口来创建、初始化和连接socket。
     * 这样也就允许HttpClient的用户在代码运行时，指定socket初始化的代码。
     * PlainConnectionSocketFactory是默认的创建、初始化明文socket(不加密)的工厂类
     * 创建socket和使用socket连接到目标主机这两个过程是分离的，所以我们可以在连接发生阻塞时，关闭socket连接。
     *
     * LayeredConnectionSocketFactory是ConnectionSocketFactory的拓展接口。
     * 分层socket工厂类可以在明文socket的基础上创建socket连接。分层socket主要用于在代理服务器之间创建安全socket。
     *
     * HttpClient使用SSLSocketFactory这个类实现安全socket，SSLSocketFactory实现了SSL/TLS分层。
     * 请知晓，HttpClient没有自定义任何加密算法。它完全依赖于Java加密标准(JCE)和安全套接字
     */
    public static void socketFactory()throws Exception{
        HttpClientContext clientContext = HttpClientContext.create();
        PlainConnectionSocketFactory sf = PlainConnectionSocketFactory.getSocketFactory();
        Socket socket = sf.createSocket(clientContext);
        int timeout = 1000; //ms
        HttpHost target = new HttpHost("www.yeetrack.com");
        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getByName("www.yeetrack.com") , 80);
        //connectSocket源码中，实际没有用到target参数
        sf.connectSocket(timeout, socket, target, remoteAddress, null, clientContext);

    }

    /**
     * case 7: SSL/TLS定制
     * HttpClient使用SSLSocketFactory来创建ssl连接。SSLSocketFactory允许用户高度定制。
     * 它可以接受javax.net.ssl.SSLContext这个类的实例作为参数，来创建自定义的ssl连接。
     */
    public static void customSSL() throws Exception{
        HttpClientContext clientContext = HttpClientContext.create();
        KeyStore myTrustStore =  KeyStore.getInstance(KeyStore.getDefaultType());
        SSLContext sslContext = SSLContexts.custom().useTLS()
                .loadTrustMaterial(myTrustStore).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
    }

    /**
     * Case 8: 对代理的设置
     * 尽管，HttpClient支持复杂的路由方案和代理链，它同样也支持直接连接或者只通过一跳的连接。
     */
    public static void setProxy(){
        //方法一：使用代理服务器最简单的方式就是，指定一个默认的proxy参数。
        HttpHost proxy = new HttpHost("someproxy", 8080);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();
        //方法二：我们也可以让HttpClient去使用jre的代理服务器。
        SystemDefaultRoutePlanner routePlanner2 = new SystemDefaultRoutePlanner(
                ProxySelector.getDefault());
        CloseableHttpClient httpclient2 = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();
        //方法三：可以手动配置RoutePlanner，这样就可以完全控制Http路由的过程
        HttpRoutePlanner routePlanner3 = new HttpRoutePlanner() {

            public HttpRoute determineRoute(
                    HttpHost target,
                    HttpRequest request,
                    HttpContext context) throws HttpException {
                return new HttpRoute(target, null,  new HttpHost("someproxy", 8080),
                        "https".equalsIgnoreCase(target.getSchemeName()));
            }

        };
    }
}

