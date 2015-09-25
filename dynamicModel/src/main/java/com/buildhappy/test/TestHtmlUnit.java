package com.buildhappy.test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by caijianfu on 15/9/24.
 */
public class TestHtmlUnit {
    private static final Log LOG = LogFactory.getLog(TestHtmlUnit.class);
    private static String  url = "http://www.bupt.edu.cn/inc/jquery-1.9.1.min.js";

    public static void main(String[] args){
        long start , end , time , globalStart = System.currentTimeMillis();
        /*
        start = System.currentTimeMillis();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        try {
            HttpResponse resp = httpclient.execute(httpget);

            EntityUtils.toString(resp.getEntity());
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        time = end - start;
        System.out.println("HttpClient consume tiem:" + time);
        */

        /**/
        start = System.currentTimeMillis();
        List alertHandler = new LinkedList();
        WebClient client = new WebClient(BrowserVersion.FIREFOX_24);//CHROME);
        client.getOptions().setActiveXNative(false);
        client.getOptions().setAppletEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setGeolocationEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setPopupBlockerEnabled(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.setAlertHandler(new CollectingAlertHandler(alertHandler));//将JavaScript中alert标签产生的数据保存在一个链表中
        end = System.currentTimeMillis();

        time = end - start;
        LOG.info("Init comsume time:" + time);


        start = System.currentTimeMillis();
        HtmlPage page = null;
        for(int i = 0; i < 10; i++){
            try {
                page = client.getPage(url);

                //client.closeAllWindows();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //System.out.println(page.asText());
        String headers = page.getWebResponse().getResponseHeaderValue("Last-Modified");
        System.out.println(headers);
        end = System.currentTimeMillis();
        time = end - start;
        LOG.info("Execute comsume time:" + time);
        time = System.currentTimeMillis() - globalStart;
        LOG.info("Total comsume time:" + time);

    }
}
