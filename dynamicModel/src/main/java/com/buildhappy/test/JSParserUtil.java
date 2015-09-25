package com.buildhappy.test;

/**
 * 对于动态网页，如果无法获取apk的下载链接，我们可以用浏览器模拟相应的下载操作(比如点击下载按钮)，
 * 然后从获取response的url，
 * 该url一般是ajax的url(即通过该url可以获取该apk的json数据，这些数据里面会有该apk的下载链接信息)
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;

public class JSParserUtil {
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(JSParserUtil.class);

	/**
	 * @param clickOfXpath
	 *            :页面待点击按钮的xpath表达式
	 * @param index
	 *            :
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
		;
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

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException{
//		String url = "http://app.flyme.cn/games/public/detail?package_name=com.cyou.cx.mtlbb.mz";
//		MyNicelyResynchronizingAjaxController ajaxController = new MyNicelyResynchronizingAjaxController();
		
		long start = System.currentTimeMillis();
		System.out.println(start);
		String url = "http://localhost:8080/test/test.html";
		//String url2 = "http://www.wandoujia.com/apps/com.itings.myradio";
		url = "http://m.163.com/android/software/32pk7e.html";
		url = "http://www.wandoujia.com/apps/com.tencent.mobileqq";
		url = "http://www.anzhi.com/soft_2123520.html#";
		//url = "http://www.anzhi.com/soft_2105031.html#";
		url = "http://zhushou.360.cn/detail/index/soft_id/77208";
		url = "http://www.diyiapp.com/search/?type=&keyword=qq";
		//百度网盘
		url = "http://pan.baidu.com/s/1ntmPo73";

		url = "http://dl.pconline.com.cn/download/61068.html";
		List alertHandler = new LinkedList();
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
		File file = new File("data.txt");
		FileWriter writer = new FileWriter(file);
		JavaScriptEngine engine = new JavaScriptEngine(webClient);
		webClient.setJavaScriptEngine(engine);
		//page = webClient.getPage(url);
		for(int i = 0; i < 1; i++){
			page = webClient.getPage(url);
			//page.executeJavaScript(sourceCode)
			HtmlElement htmlElement = (HtmlElement)(page.getByXPath("//a[@class='btn sbDownload']").get(0));//get download url from www.anzhi.com
		
			//执行页面中的函数genLink()
			String javaScriptCode = "genLink('afa')";
			ScriptResult result = page.executeJavaScript(javaScriptCode);
			//javaScriptCode = "genLink('afa')";
			System.out.println("getNewPage:"+ result.getNewPage().toString());
			System.out.println("result:" + result.getJavaScriptResult().toString());
			
			//System.out.println(htmlElement.getScriptObject().callMethod((Scriptable) page, "genLink",null));
			System.out.println();
			//System.out.println("url:" + htmlElement.click().getUrl());
			String htmlPage = page.asXml();
			writer.write(htmlPage);
			writer.flush();
			writer.close();
			//LOGGER.info(page.asXml());
			//System.out.println(htmlElement.click(event));
			
			//System.out.println(page.asXml());
			//HtmlElement htmlElement = (HtmlElement)page.getByXPath("//p[@class='m-t15']/a/@href").get(0);
			//System.out.println((DomAttr)(page.getByXPath("//p[@class='m-t15']/a/@href").get(0)));
			//DomAttr dom = (DomAttr)(page.getByXPath("//p[@class='m-t15']/a/@href").get(0));//163
			//System.out.println(dom.getValue());
			
			//HtmlElement htmlElement = (HtmlElement)(page.getByXPath("//a[@class='down-button sofe-button']").get(0));//get download url from www.anzhi.com
			//System.out.println("url:" + htmlElement.click().getUrl());
			//System.out.println(dom.getValue());
			System.out.println("第" + i + "次：" + (System.currentTimeMillis() - start)/1000);
			start = System.currentTimeMillis();
		}

		//System.out.println(page.getPageEncoding());
		//System.out.println(page.asXml());
		//System.out.println(webClient.getJavaScriptEngine().getJavaScriptExecutor());
		
		//HtmlElement htmlElement = (HtmlElement) page.getByXPath("//div[@class='detail_down']/a").get(0);
		//htmlElement.click();
	}
	// page = webClient.getPage(url2);
	// System.out.println(page.asText());
	// System.out.println(page.getByXPath("//div[@class='comments']").get(0).toString());
	//
	//
	// System.out.println(htmlElement.asText());
	// Page resultPage = htmlElement.click();//单击后返回一个Page对象
	// System.out.println(resultPage.getUrl());
	//
	// List<WebWindow> windows = webClient.getWebWindows();
	// Iterator it = windows.iterator();
	// WebWindow webWindow = null;
	// while(it.hasNext()){
	// webWindow = (WebWindow) it.next();
	// System.out.println("InnerWidth: " + webWindow.getInnerWidth());
	// System.out.println("EnclosedPage url: " +
	// webWindow.getEnclosedPage().getUrl());
	// System.out.println("History: " + webWindow.getHistory());
	// }

	// webClient.getPage(webWindow, new WebRequest(new URL(url) ,
	// BrowserVersion.CHROME.getHtmlAcceptHeader()));
	// windows = webClient.getWebWindows();
	// it = windows.iterator();
	// while(it.hasNext()){
	// webWindow = (WebWindow) it.next();
	// System.out.println("InnerWidth: " + webWindow.getInnerWidth());
	// System.out.println("EnclosedPage url: " +
	// webWindow.getEnclosedPage().getUrl());
	//
	// }
	// WebWindow webWindow = TopLevelWindow;
	// System.out.println(page.getHead());
	public static void executeFunInPage(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException{
		url = "http://dl.pconline.com.cn/download/61068.html";
		
		List alertHandler = new LinkedList();;
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);//CHROME);
		webClient.setAjaxController(new MyNicelyResynchronizingAjaxController());
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setTimeout(3500);
		webClient.getOptions().setThrowExceptionOnScriptError(true);
		webClient.getOptions().setCssEnabled(true);
		webClient.getOptions().isRedirectEnabled();
		webClient.setAlertHandler( new CollectingAlertHandler(alertHandler));//将JavaScript中alert标签产生的数据保存在一个链表中
		
		HtmlPage page = null;
		page = webClient.getPage(url);
		//执行页面中的函数genLink()
		String javaScriptCode = "genLink('afa')";
		ScriptResult result = page.executeJavaScript(javaScriptCode);
		//javaScriptCode = "genLink('afa')";
		System.out.println("getNewPage:"+ result.getNewPage().toString());
		System.out.println("result:" + result.getJavaScriptResult().toString());
	}
}
