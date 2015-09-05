package com.buildhappy;

import java.util.List;

public class Apk2 {
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
	
	public static class ApkBuilder{
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
		
		public ApkBuilder(){}
		
		public ApkBuilder setAppName(String name){
			this.appName = name;
			return this;
		}
		
		public ApkBuilder setAppMetaUrl(String appMetaUrl){
			this.appMetaUrl = appMetaUrl;
			return this;
		}
		
		public Apk2 builder(){
			return new Apk2(this);
		}
		
	}
	
	private Apk2(ApkBuilder apkBuilder){
		this.appName = apkBuilder.appName;
	}
	
	public static void main(String[] args){
		Apk2 apk = new Apk2.ApkBuilder().setAppMetaUrl("fafa").setAppName("faf").builder();
	}
}
