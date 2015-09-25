package com.buildhappy.test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by baifan on 15/9/24.
 */
public class FetchTest {
    private final static Logger LOG = LoggerFactory.getLogger(FetchTest.class);
    private final static String URL = "http://cis.sankuai.com/open/home";
    private final static ExecutorService executors = Executors.newCachedThreadPool();
    final RateLimiter rateLimiter = RateLimiter.create(500);
    public void testHtmlUnit() {
        while(true) {
            rateLimiter.acquire();
            executors.submit(new HtmlUnitTask());
        }
    }
    public void testHttpClient() {
        while(true) {
            rateLimiter.acquire();
            executors.submit(new HttpClientTask());
        }
    }
    private static class HtmlUnitTask implements Runnable {
        public void run() {
            WebClient client = new WebClient();
            client.getOptions().setActiveXNative(false);
            client.getOptions().setAppletEnabled(false);
            client.getOptions().setCssEnabled(false);
            client.getOptions().setGeolocationEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setPopupBlockerEnabled(false);
            client.getOptions().setPrintContentOnFailingStatusCode(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setThrowExceptionOnScriptError(false);
            Page page = null;
            try {
                page = client.getPage(URL);
                client.closeAllWindows();
            } catch (IOException e) {
                LOG.error("Failed", e);
            }
            page.getWebResponse().getContentAsString();
        }
    }

    private static class HttpClientTask implements Runnable {
        public void run() {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(URL);
            try {
                HttpResponse resp = client.execute(httpget);

                EntityUtils.toString(resp.getEntity());
                client.close();
            } catch (IOException e) {
                LOG.error("Failed", e);
            }
        }
    }
    public static void main (String[] args) {
        FetchTest test = new FetchTest();
        if("htmlunit".equalsIgnoreCase(args[args.length - 1])) {
            LOG.info("starting htmlunit test");
            test.testHtmlUnit();
        } else if ("httpclient".equalsIgnoreCase(args[args.length - 1])) {
            LOG.info("starting httpclient test");
            test.testHttpClient();
        } else {
            LOG.error("invalid arguments, exit");
            return;
        }
    }
}