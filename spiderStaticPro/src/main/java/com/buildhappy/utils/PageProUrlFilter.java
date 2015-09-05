package com.buildhappy.utils;
/**
 * filter the url of the page util
 * @author buildhappy
 *
 */
public class PageProUrlFilter {
	public static boolean isUrlReasonable(String url){
		String temp = url.toLowerCase();
		if(temp.endsWith(".zip") || temp.endsWith(".apk") || temp.endsWith(".jpeg")
				|| temp.endsWith(".exe") || temp.endsWith(".jpg") || temp.endsWith(".rar") 
				|| temp.endsWith(".gif") || temp.contains("#") || temp.endsWith(".png")
				|| temp.endsWith(".doc")|| temp.endsWith(".xml"))
			return false;
		return true;
	}
	public static void main(String[] args) {

	}

}