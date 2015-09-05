package com.buildhappy.processor.imp;

import com.buildhappy.Page;
import com.buildhappy.pipeline.mybatis.domain.Apk;
import com.buildhappy.selector.Html;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 网易应用中心[中国] app搜索抓取
 * url:http://m.163.com/android/search.html?platform=2&query=DOTA
 *
 * @version 1.0.0
 */
public class PagePro163_Detail {


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PagePro163_Detail.class);

    public static Apk getApkDetail(Page page){
        // 获取dom对象
        Html html = page.getHtml();

        // 找出对应需要信息
        String appDetailUrl = page.getUrl().toString();
        String appName = html.xpath("//div[@class='sect-main-s-inner p-t15']/h1/span/text()").toString();
        String appVersion = StringUtils.substringAfterLast(html.xpath("//div[@class='t-c p-t20']/p[6]/text()").toString(), "：");
        String appDownloadUrl = html.xpath("//div[@class='t-c p-t20']/p[1]/a/@href").toString();
        String osPlatform = null;
        String appSize = StringUtils.substringAfterLast(html.xpath("//div[@class='t-c p-t20']/p[5]/text()").toString(), "：");
        String appUpdateDate = null;
        String appType = null;
        String appDescription = html.xpath("//div[@id='app-desc']/text()").get();
        List<String> appScreenshot = Lists.newArrayList(StringUtils.substringsBetween(StringUtils.substringBetween(html.get(), "shotData: ", "],") + "]", "\"ImgUrl\":\"", "\""));
        String appTag = null;
        String appCategory = null;
        String appCommentUrl = "http://m.163.com/" + StringUtils.substringBetween(html.get(), "commentUrl:'", "'").replaceAll("pagesize=\\d", "pagesize=5000");
        String appComment = null;
        String dowloadNum = null;

        Apk apk = null;
        if (null != appName && null != appDownloadUrl) {
            apk = new Apk(appName, appDetailUrl, appDownloadUrl, osPlatform, appVersion, appSize, appUpdateDate, null != appType ? appType : "APK");
            apk.setAppDescription(appDescription);
            apk.setAppScreenshot(appScreenshot);
            apk.setAppCommentUrl(appCommentUrl);
            apk.setAppComment(appComment);
            apk.setAppDownloadTimes(dowloadNum);
            apk.setAppCategory(appCategory);
            apk.setAppTag(appTag);
        }

        LOGGER.debug("name:{}, version: {}, url:{}, size: {}, appType: {}, os: {}, date:{}, downlaodNum:{}, , appTag:{}, appCategory:{}" +
                        ", appScreenhost:{}, appCommentUrl:{}, appComment:{}, appDescription:{}", appName, appVersion, appDownloadUrl,
                appSize, appType, osPlatform, appUpdateDate, dowloadNum, appTag, appCategory, appScreenshot, appCommentUrl, appComment, appDescription);

        return apk;
    }
}
