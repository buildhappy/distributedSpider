package com.buildhappy.processor.imp;

import com.buildhappy.Page;
import com.buildhappy.Site;
import com.buildhappy.pipeline.mybatis.domain.Apk;
import com.buildhappy.processor.PageProcessor;
import com.buildhappy.selector.Html;
import com.buildhappy.utils.PageProUrlFilter;
import com.google.common.collect.Sets;

import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * 网易应用中心[中国] app搜索抓取
 * url:http://m.163.com/android/search.html?platform=2&query=DOTA
 *
 * @version 1.0.0
 */
public class PagePro163 implements PageProcessor {

    // 日志管理对象
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PagePro163.class);

    // 定义网站编码，以及间隔时间
    Site site = Site.me().setCharset("utf-8").setRetryTimes(2).
			setSleepTime(3000);

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param page
     */
    @Override
    public Apk process(Page page) {
        LOGGER.debug("crawler url: {}", page.getUrl());

        // 获取搜索页面
        if (page.getUrl().regex("http://m\\.163\\.com/android/").match()) {
            LOGGER.debug("match success, url:{}", page.getUrl());

            List<String> urlList = page.getHtml().links().regex("http://m\\.163\\.com/android/.*").all();
            // http://game.3533.com/ruanjian/1071.htm
            Iterator<String> iter = Sets.newHashSet(urlList).iterator();
            while (iter.hasNext()) {
            	String url = iter.next();
    			if(PageProUrlFilter.isUrlReasonable(url)){
    				page.addTargetRequest(url);
    			}
            }
            // 打印搜索结果url
            LOGGER.debug("app info results urls: {}", page.getTargetRequests());
        }

        // 获取信息
        if (page.getUrl().regex("http://m.163.com/android/software/*").match()) {
            Html html = page.getHtml();
			Apk apk = PagePro163_Detail.getApkDetail(page);
			
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
