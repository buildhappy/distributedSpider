package com.buildhappy.dynamicModel.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.buildhappy.test.MyNicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;


public class DynamicUtil implements Runnable{
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private ZFrame sendTo;
    private String data;//the static page to parse
    Socket worker;
    ZMsg msg;
    String clickButton;//the button to click
    /**
     * DynamicUtil constructor
     * @param worker
     * @param msg
     */
    public DynamicUtil(Socket worker , ZMsg msg){
    	LOGGER.info("DynamicUtil msg from broker: \n" + msg.toString());
    	this.worker = worker;
    	assert(msg.size() >= 3);
    	this.msg = msg;
    	sendTo = msg.unwrap();
    	//msg.pop();
    	ZFrame m = msg.pop();
    	data = m.toString();
    	LOGGER.info("DynamicUtil from broker data:" + data);
    	if(!msg.isEmpty()){
    		clickButton = msg.popString();
    	}
    }
    
	/**
	 * get the ajax url from the click button
	 * @param clickOfXpath:页面待点击按钮的xpath表达式
	 * @param index
	 * @return List<String>:链表的第一个信息是页面的title，以后的信息是所有的ajax的url
	 */
	public static List<String> getAjaxUrl(String targetUrl,
			String clickOfXpath, int index)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException {
		// TARGET_URL =
		// "http://app.flyme.cn/apps/public/detail?package_name=com.myzaker.zaker_phone_smartbar";
		List<String> urls = new LinkedList<String>();
		// 每次ajax请求时都会创建一个AjaxController对象，在该对象中可以查看ajax请求的地址
		MyNicelyResynchronizingAjaxController ajaxController = new MyNicelyResynchronizingAjaxController();

		List alertHandler = new LinkedList();
		// 模拟一个浏览器
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
		// HtmlUnitDriver
		// 设置webClient的相关参数
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.setAjaxController(ajaxController);
		webClient.getOptions().setTimeout(35000);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.setAlertHandler(new CollectingAlertHandler(alertHandler));// 将JavaScript中alert标签产生的数据保存在一个链表中

		// 模拟浏览器打开一个目标网址
		HtmlPage rootPage = webClient.getPage(targetUrl);
		urls.add(rootPage.getTitleText());
		urls.add(ajaxController.getVisitUrl());
		// System.out.println("url1:" + url);
		HtmlElement elementA = (HtmlElement) rootPage.getByXPath(clickOfXpath)
				.get(index);
		Page page = elementA.click();
		urls.add(ajaxController.getVisitUrl());
		return urls;
	}
	public String getParsedPage(){
		List<String> alertHandler = new LinkedList<String>();;
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);//CHROME);
		webClient.setAjaxController(new MyNicelyResynchronizingAjaxController());
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setTimeout(3500);
		webClient.getOptions().setThrowExceptionOnScriptError(true);
		webClient.getOptions().setCssEnabled(true);
		webClient.getOptions().isRedirectEnabled();
		webClient.setAlertHandler( new CollectingAlertHandler(alertHandler));//将JavaScript中alert标签产生的数据保存在一个链表中
		//webClient.getOptions().setThrowExceptionOnScriptError(false);
		HtmlPage page = null;
		JavaScriptEngine engine = new JavaScriptEngine(webClient);
		webClient.setJavaScriptEngine(engine);
		try {
			page = webClient.getPage(data);
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(page != null){
			return page.asXml();
		}
		return null;
	}
	public void run() {
		LOGGER.info("DynamicUtil run()");
		String result = "result from dynamic worker";//getParsedPage();
		ZMsg msg = new ZMsg();
		msg.addFirst(result);
		//msg.addFirst(new byte[0]);
		msg.wrap(sendTo);
		msg.addFirst(MDP.W_REPLY.newFrame());
		msg.addFirst(MDP.DYNAMIC_WORKER.newFrame());
		msg.addFirst(new byte[0]);//这就是dealer与req的不同之处，req在此处会自动加入一个空帧
		LOGGER.info("I:sending reply to broker\n" + msg.toString());
		System.out.println("I:sending reply to broker\n" + msg.toString());
		//msg.dump(log.out());
		msg.send(worker);
		msg.destroy();
	}
}
