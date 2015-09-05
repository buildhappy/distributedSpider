package com.buildhappy.processor.imp;
import com.buildhappy.Page;
import com.buildhappy.Site;
import com.buildhappy.pipeline.mybatis.domain.Apk;
import com.buildhappy.processor.PageProcessor;
import com.buildhappy.selector.Html;
import com.buildhappy.utils.PageProUrlFilter;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * hiapk[中国] app搜索抓取
 * url:http://apk.hiapk.com/search?key=mt&pid=0
 * id:46
 * @version 1.0.0
 */
public class PageProHiApk implements PageProcessor {

    // 日志管理对象
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PageProHiApk.class);

    // 定义网站编码，以及间隔时间
    Site site = Site.me().setCharset("utf-8").setRetryTimes(2).setSleepTime(3000);

    public Apk process(Page page) {
        LOGGER.info("crawler url: {}", page.getUrl());

        // 获取搜索页面
        if (page.getUrl().regex("http://apk\\.hiapk\\.com.*").match()) {
            LOGGER.debug("match success, url:{}", page.getUrl());

            // 获取详细链接，以及分页链接
            //List<String> urlList = page.getHtml().links().regex("http://apk\\.hiapk\\.com/?|apps/|games/|appinfo/.*").all();
            List<String> urlList = page.getHtml().links().regex("http://apk\\.hiapk\\.com/.*").all();
    		for (String temp : urlList) {
    			if(PageProUrlFilter.isUrlReasonable(temp))				
    				page.addTargetRequest(temp);
    		}

            // 打印搜索结果url
            LOGGER.debug("app info results urls: {}", page.getTargetRequests());
        }

        // 获取信息
        if (page.getUrl().regex("http://apk\\.hiapk\\.com/appinfo/.*").match()) {
            Html html = page.getHtml();
			Apk apk = PageProHiApk_Detail.getApkDetail(page);
			
			page.putField("apk", apk);
			if(page.getResultItems().get("apk") == null){
				page.setSkip(true);
			}
		}else{
			page.setSkip(true);
		}

        return null;
    }

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }

	@Override
	public List<Apk> processMulti(Page page) {
		// TODO Auto-generated method stub
		return null;
	}
}