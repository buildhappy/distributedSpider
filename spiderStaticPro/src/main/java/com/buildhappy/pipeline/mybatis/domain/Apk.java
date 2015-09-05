package com.buildhappy.pipeline.mybatis.domain;

import java.util.List;
import java.util.UUID;

/**
 * the class for apk
 * @author Administrator
 * 	    agentId : agent_id
        channelId : channel_id
        header : header
        status : status // status 可能是running=3，done=4，也可能是failed=0
        details : {
            // 一个渠道中有n个应用，n=0
            appName : app_name
            appVersion : app_version
            appVenderName : app_vender_name
            appPackageName : app_package_name
            appType : app_type
            appSize : app_size
            appDownloadTimes: app_download_times
            appTsChannel : app_ts_channel // app上传时间
            osPlatform : os_platform // 适合的操作系统
            appMetaUrl : app_meta_url
            cookie : cookie // 一个应用一个cookie
            appDownloadUrl : app_download_url
            appDescription : app_description
            appCommentUrl : app_comment_url // 没有则为空
            appComment : app_comment // 没有则为空
            appScreenshot: 	app的屏幕截图(List<String>，取屏幕截图的url)
            appTag:			app的应用标签(String)
            appCategory:	app的应用类别(String)
        }
 *
 */
public class Apk {
	private String appName;
	private String appMetaUrl;
	private String appDownloadUrl;
	private String osPlatform ;
	private String appVersion;
	private String appSize;
	private String appTsChannel;// app上传时间
	private String appType;
	private String cookie;
	private String appVenderName=null;
	private String appPackageName = null;
	private String appDownloadTimes = null;
	//新加字段
	private String appDescription = null;
	private String appCommentUrl = null;
	private String appComment = null;
	
	private List<String> appScreenshot = null;
	private String appTag = null;//应用标签
	private String appCategory = null;//应用类别
	
	public Apk(){
		
	}
	/**
	 * @param appName
	 * @param appMetaUrl
	 * @param appDownloadUrl
	 * @param osPlatform
	 * @param appVersion
	 * @param appSize
	 * @param appTsChannel
	 * @param appType
	 * @param cookie
	 * @param appComment
	 * @param appCommentUrl
	 * @param appDescription
	 */
	public Apk(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion,String appSize,String appTsChannel, String appType,String cookie,String appComment,
			String appCommentUrl,String appDescription){	
		init();
		create(appName,appMetaUrl, appDownloadUrl, osPlatform,appVersion,
				appSize,appTsChannel, appType,cookie);
		this.appComment = appComment;
		this.appCommentUrl = appCommentUrl;
		this.appDescription = appDescription;
	}
	/**
	 * create an apk object with the following parameter
	 * @param appName
	 * @param appMetaUrl
	 * @param appDownloadUrl
	 * @param osPlatform
	 * @param appVersion
	 * @param appSize
	 * @param appTsChannel
	 * @param appType
	 * @param cookie
	 */
	public Apk(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion,String appSize,String appTsChannel, String appType,String cookie){	
		init();
		create(appName,appMetaUrl, appDownloadUrl, osPlatform,appVersion,
				appSize,appTsChannel, appType,cookie);
	}
	
	/**
	 * create an apk object with the following parameter(8 parameters)
	 * @param appName
	 * @param appMetaUrl
	 * @param appDownloadUrl
	 * @param osPlatform
	 * @param appVersion
	 * @param appSize
	 * @param appTsChannel
	 * @param appType
	 */
	public Apk(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion,String appSize,String appTsChannel, String appType){
		init();
		create(appName,appMetaUrl, appDownloadUrl, osPlatform,appVersion,
				appSize,appTsChannel, appType,null);
	}
	
	/**
	 * create an apk object with the following parameter
	 * @param appName
	 * @param appMetaUrl
	 * @param appDownloadUrl
	 * @param osPlatform
	 * @param appVersion
	 * @param appSize
	 * @param appTsChannel
	 */
	public Apk(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion,String appSize,String appTsChannel){
		init();
		create(appName,appMetaUrl, appDownloadUrl, osPlatform,appVersion,
				appSize,appTsChannel, null,null);
	}
	
	/**
	 * create an apk object with the following parameter
	 * @param appName
	 * @param appMetaUrl
	 * @param appDownloadUrl
	 * @param osPlatform
	 * @param appVersion
	 * @param appSize
	 */
	public Apk(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion,String appSize){
		init();
		create(appName,appMetaUrl, appDownloadUrl, osPlatform,appVersion,
				appSize,null, null,null);
	}
	/**
	 * create an apk object with the following parameter
	 * @param appName
	 * @param appMetaUrl
	 * @param appDownloadUrl
	 * @param osPlatform
	 * @param appVersion
	 */
	public Apk(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion){
		init();
		create(appName,appMetaUrl, appDownloadUrl, osPlatform,appVersion,
				null,null, null,null);
	}
	private void init(){
		//this.appCrawlTime = String.valueOf(System.currentTimeMillis());
	}
	public void create(String appName,String appMetaUrl,String appDownloadUrl,String osPlatform ,
			String appVersion,String appSize,String appTsChannel, String appType,String cookie){
		this.appName = appName;
		this.appVersion = appVersion;
		this.appType = appType;
		this.appSize = appSize;
		this.appTsChannel = appTsChannel;
		this.osPlatform = osPlatform;
		this.appMetaUrl = appMetaUrl;
		this.appDownloadUrl = appDownloadUrl;
		this.cookie = cookie;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getAppSize() {
		return appSize;
	}

	public void setAppSize(String appSize) {
		this.appSize = appSize;
	}

	public String getAppTsChannel() {
		return appTsChannel;
	}

	public void setAppTsChannel(String appTsChannel) {
		this.appTsChannel = appTsChannel;
	}

	public String getOsPlatform() {
		return osPlatform;
	}

	public void setOsPlatform(String osPlatform) {
		this.osPlatform = osPlatform;
	}

	public String getAppMetaUrl() {
		return appMetaUrl;
	}

	public void setAppMetaUrl(String appMetaUrl) {
		this.appMetaUrl = appMetaUrl;
	}

	public String getAppDownloadUrl() {
		return appDownloadUrl;
	}

	public void setAppDownloadUrl(String appDownloadUrl) {
		this.appDownloadUrl = appDownloadUrl;
	}
	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	public String getAppVenderName() {
		return appVenderName;
	}

	public void setAppVenderName(String appVenderName) {
		this.appVenderName = appVenderName;
	}

	public String getAppPackageName() {
		return appPackageName;
	}

	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}
	
	public String getAppDownloadTimes() {
		return appDownloadTimes;
	}

	public void setAppDownloadTimes(String downloadTimes) {
		this.appDownloadTimes = downloadTimes;
	}
	public String getAppDescription() {
		return appDescription;
	}

	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

	public String getAppCommentUrl() {
		return appCommentUrl;
	}

	public void setAppCommentUrl(String appCommentUrl) {
		this.appCommentUrl = appCommentUrl;
	}

	public String getAppComment() {
		return appComment;
	}

	public void setAppComment(String appComment) {
		this.appComment = appComment;
	}
	
	public List<String> getAppScreenshot() {
		return appScreenshot;
	}
	public void setAppScreenshot(List<String> appScreenshot) {
		this.appScreenshot = appScreenshot;
	}
	public String getAppTag() {
		return appTag;
	}
	public void setAppTag(String appTag) {
		this.appTag = appTag;
	}
	public String getAppCategory() {
		return appCategory;
	}
	public void setAppCategory(String appCategory) {
		this.appCategory = appCategory;
	}
	
	@Override
	public String toString(){
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("name:" + this.appName);
		strBuilder.append(",appVersion:" + this.appVersion);
		strBuilder.append(",appSize:" + this.appSize + "\n");
		//strBuilder.append("\nappType:" + this.appType);
		//strBuilder.append("\nosPlatform:" + this.osPlatform);
		//strBuilder.append("\nappDownloadUrl:" + this.appDownloadUrl);
		//strBuilder.append("\nappComment:" + this.getAppComment());
		//strBuilder.append("\nappCommentUrl:" + this.getAppCommentUrl());
		return strBuilder.toString();
	}
	@Override
	public boolean equals(Object o){
		Apk apk = (Apk)o; 
		return (this.appName.equals(apk.appName) && this.appDownloadUrl.equals(apk.appDownloadUrl));
	}
	public static void main(String[] args){
		System.out.println(UUID.randomUUID().toString());
		Apk apk1 = new Apk();
		apk1.setAppName("hh");
		apk1.setAppDownloadUrl("hh");
		
		Apk apk2 = new Apk();
		apk2.setAppName("hh");
		apk2.setAppDownloadUrl("hh");
		System.out.println(apk2.equals(apk1));
	}
}
