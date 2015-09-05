package com.buildhappy.processor.imp;
/**
 * 安智网 http://www.anzhi.com/index.html
 * Anzhi #127
 * 下载apk的url需要手动构造
 * @author DMT
 */
import java.util.List;

import com.buildhappy.Page;
import com.buildhappy.pipeline.mybatis.domain.Apk;

import org.slf4j.LoggerFactory;


public class Anzhi_Detail {
	private static final org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(Anzhi_Detail.class);

	public static Apk getApkDetail(Page page) {
		Apk apk = null;
		String appName = null; // app名字
		String appDetailUrl = null; // 具体页面url
		String appDownloadUrl = null; // app下载地址
		String osPlatform = null; // 运行平台
		String appVersion = null; // app版本
		String appSize = null; // app大小
		String appUpdateDate = null; // 更新日期
		String appType = null; // 下载的文件类型 apk？zip？rar？
		String appVenderName = null; // app开发者 APK这个类中尚未添加
		String appDownloadedTime = null; // app的下载次数
		String appDescription = null; // app的详细描述
		List<String> appScrenshot = null; // app的屏幕截图
		String appTag = null; // app的应用标签
		String appCategory = null; // app的应用类别
		
		
		
		appName=page.getHtml().xpath("//div[@class='detail_description']/div[1]/h3/text()").toString();			
		if(appName == null)	return null;
		
		appDetailUrl = page.getUrl().toString();
		
		String pageidString=appDetailUrl.substring(appDetailUrl.indexOf("soft")+5,appDetailUrl.indexOf("html")-1);
			appDownloadUrl = "www.anzhi.com/dl_app.php?s="+pageidString+"&n=5";
		
		String platFormString =page.getHtml().xpath("//ul[@id='detail_line_ul']/li[5]/text()").toString();
			
		osPlatform = platFormString.substring(platFormString.indexOf("：")+1,platFormString.length());
	
		String versionString = page.getHtml().xpath("//span[@class='app_detail_version']/text()").toString();
			appVersion = versionString.substring(versionString.indexOf("(")+1,versionString.lastIndexOf(")"));
		
		String sizeString = page.getHtml().xpath("//ul[@id='detail_line_ul']/li[4]/span/text()").toString();
			appSize = sizeString.substring(sizeString.indexOf("：")+1,sizeString.length());
		
		String updatedateString = page.getHtml().xpath("//ul[@id='detail_line_ul']/li[3]/text()").toString();
			appUpdateDate = updatedateString.substring(updatedateString.indexOf("：")+1,updatedateString.length());
		
	
		appType = "apk";
		
		String venderString = page.getHtml().xpath("//ul[@id='detail_line_ul']/li[7]/text()").toString();
		if(venderString != null && venderString.contains("："))
			appVenderName=venderString.substring(venderString.indexOf("：")+1,venderString.length());
			
			
		String DownloadedTimeString = page.getHtml().xpath("//ul[@id='detail_line_ul']/li[4]/span/text()").toString();
			appDownloadedTime = DownloadedTimeString.substring(DownloadedTimeString.indexOf("：")+1,DownloadedTimeString.length());		
		
		String descriptionString = page.getHtml().xpath("//div[@class='app_detail_infor']/p/text()").toString();
			appDescription = descriptionString.replace("\n", "");
			appDescription = appDescription.replace(" ", "");
		appCategory = page.getHtml().xpath("//div[@class='title']/h2/a/text()").toString();
		if(appCategory == null) appCategory = page.getHtml().xpath("//div[@class='title']/h3/text()").toString();
		appScrenshot = page.getHtml().xpath("//div[@class='section-body']//img/@src").all();
//		System.out.println("appName=" + appName);
//		System.out.println("appDetailUrl=" + appDetailUrl);
//		System.out.println("appDownloadUrl=" + appDownloadUrl);
//		System.out.println("osPlatform=" + osPlatform);
//		System.out.println("appVersion=" + appVersion);
//		System.out.println("appSize=" + appSize);
//		System.out.println("appUpdateDate=" + appUpdateDate);
//		System.out.println("appType=" + appType);
//		System.out.println("appVenderName=" + appVenderName);
//		System.out.println("appDownloadedTime=" + appDownloadedTime);
//		System.out.println("appDescription=" + appDescription);
//		System.out.println("appTag=" + appTag);
//		System.out.println("appScrenshot=" + appScrenshot);
//		System.out.println("appCategory=" + appCategory);

		if (appName != null && appDownloadUrl != null) {
			apk = new Apk(appName, appDetailUrl, appDownloadUrl, osPlatform,
					appVersion, appSize, appUpdateDate, appType, null);
			// Apk(String appName,String appMetaUrl,String appDownloadUrl,String
			// osPlatform ,
			// String appVersion,String appSize,String appTsChannel, String
			// appType,String cookie){
			apk.setAppDownloadTimes(appDownloadedTime);
			apk.setAppVenderName(appVenderName);
			apk.setAppTag(appTag);
			apk.setAppScreenshot(appScrenshot);
			apk.setAppDescription(appDescription);
			apk.setAppCategory(appCategory);

		} else
			return null;

		return apk;
	}
	
	
}