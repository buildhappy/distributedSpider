package com.buildhappy.processor.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buildhappy.Page;
import com.buildhappy.Site;
import com.buildhappy.pipeline.mybatis.domain.Apk;
import com.buildhappy.processor.PageProcessor;
import com.buildhappy.selector.Html;
import com.google.common.collect.Sets;

public class PagePro360 implements PageProcessor{
	//Site site = Site.me().setCharset("utf-8").setRetryTimes(2).setSleepTime(0);
	Site site = Site.me().setCharset("utf-8").setRetryTimes(1).setSleepTime(3000);
	private Logger LOGGER = LoggerFactory.getLogger(PagePro360.class);

	public Apk process(Page page) {
		LOGGER.info("PagePro360 process(Page page)");
		List<String> urls =page.getHtml().links().regex("(http://zhushou\\.360\\.cn/.*)").all() ;
		Set<String> cacheSet = Sets.newHashSet();
		cacheSet.addAll(urls);
		
		//构造分页
		//http://zhushou.360.cn/list/index/cid/1
		//if(page.getUrl().regex("(http://zhushou\\.360\\.cn/detail/list/index/.*)").match()){
		if(page.getRequest().getUrl().equals("http://zhushou.360.cn/list/index/cid/1")||
				page.getRequest().getUrl().equals("http://zhushou.360.cn/list/index/cid/2")){
			String pageStr=page.getHtml().regex("(pg\\.pageCount\\s=\\s\\w+)").toString();
			int pageCount=Integer.parseInt(pageStr.substring(15));
			List<String> url1=new ArrayList<String>();
			for(int i= 2;i<=pageCount;i++){
				url1.add(page.getRequest().getUrl()+"?page="+i);
			}
			page.addTargetRequests(url1);
		}
		//剔除锚点.*?#.*
		//#expand,#next,#prev,#comment,#nogo,#guess-like,#btn-install-now-log,#comment-list,#report
		for(String url : cacheSet){
			if(url.toString().endsWith("#expand")||url.toString().endsWith("#next") || url.toString().endsWith("#prev")
					||url.toString().endsWith("#comment")||url.toString().endsWith("#nogo") || url.toString().endsWith("#guess-like")
					||url.toString().endsWith("#btn-install-now-log") || url.toString().endsWith("#comment-list")
					|| url.toString().endsWith("#report")){
				//LOGGER.error("anchor:" + url.toString());
			}else{
				//LOGGER.info(url.toString());
				page.addTargetRequest(url);
			}
		}
		
		//提取页面信息
		if(page.getUrl().regex("(http://zhushou\\.360\\.cn/detail/index/soft_id/.*)").match()){
			//LOGGER.info("get meta info");
			Html html = page.getHtml();
	        String appDetailUrl = page.getUrl().toString();
	        String appName = html.xpath("//dl[@class='clearfix']/dd/h2/span/text()").toString();
	        String appVersion = html.xpath("//div[@class='base-info']/table/tbody/tr[2]/td[1]/text()").get();
	        String appDownloadUrl = StringUtils.substringBetween(html.get(), "'downloadUrl': '", "'");
	        String osPlatform = html.xpath("//div[@class='base-info']/table/tbody/tr[2]/td[2]/text()").get();
	        String appSize = html.xpath("//div[@class='pf']/span[4]/text()").get();
	        String appUpdateDate = html.xpath("//div[@class='base-info']/table/tbody/tr[1]/td[2]/text()").get();
	        String appType = null;
	        String downcount= StringUtils.substringAfter(html.xpath("//div[@class='pf']/span[3]/text()").get(), "：");
	        String appDescription = html.xpath("//div[@class='breif']/text()").get();
	        // 评论url
	        String commontUrl = "";
	        //LOGGER.info("name:{}, version: {}, url:{}, size: {}, appType: {}, os: {}, date:{}, commontUrl:{}, "
	        		//+ "downloadNum:{}, appDesc:{}", appName, appVersion, appDownloadUrl, appSize, 
	        		//appType, osPlatform, appUpdateDate, commontUrl, downcount, appDescription);
	        Apk apk = null;
	        if (null != appName && null != appDownloadUrl) {
	            apk = new Apk(appName, appDetailUrl, appDownloadUrl, osPlatform, appVersion, appSize, appUpdateDate, null != appType ? appType : "APK");
	            apk.setAppDescription(appDescription);
	            apk.setAppDownloadTimes(downcount);
	            apk.setAppCommentUrl(commontUrl);
	            apk.setAppCommentUrl(commontUrl);
	        }
			
			page.putField("apk", apk);
			if(page.getResultItems().get("apk") == null){
				page.setSkip(true);
			}
		}else{
			page.setSkip(true);
		}
		return null;
	}

	public List<Apk> processMulti(Page page) {
		return null;
	}
	
	public Site getSite() {
		return site;
	}
	
	public static void main(String[] args){
		String url = "http://zhushou.360.cn/list/index/cid/1?page=24#expand";
//		if(url.endsWith("#expand||#next||#prev||#comment||#nogo||#guess-like||#btn-install-now-log")){
		if(url.endsWith("#expand || #next")){
			System.out.println("true");
		}
	}
}
