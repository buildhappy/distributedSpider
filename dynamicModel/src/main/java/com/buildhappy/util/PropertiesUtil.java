package com.buildhappy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * get info from properties file
 * @author buildhappy
 *
 */
public class PropertiesUtil {
	private static Properties pro = null;
	static{
		pro = new Properties();
		InputStream in = PropertiesUtil.class.getResourceAsStream("/baseInfo.properties");
		try {
			pro.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	public static String getRedisServerIp(){
		return pro.getProperty("redis.server.ip");
	}
	
	public static int getRedisServerPort(){
		return Integer.parseInt(pro.getProperty("redis.server.port"));
	}
	
	
	public static String getRouterDealerAddress(){
		return pro.getProperty("router.dealer.address");
	}
	
	//use proxy or not
	public static boolean getCrawlerProxyEnable(){
		return pro.getProperty("crawler.proxy.enable").equals("0");
	}
	
	public static String[] getCrawlerProxyHostAndPort(){
		String[] hostAndPort = pro.getProperty("crawler.proxy.host_port").split(",");		
		return hostAndPort;		
	}
	public static void main(String[] args) {
		//PropertiesUtil property = new PropertiesUtil();
		//System.out.println(property.getRetryTimes());
		System.out.println(PropertiesUtil.getRedisServerIp());
		System.out.println(PropertiesUtil.getRedisServerPort());
		System.out.println(PropertiesUtil.getRouterDealerAddress());
		System.out.println(PropertiesUtil.getCrawlerProxyEnable());
		System.out.println(PropertiesUtil.getCrawlerProxyHostAndPort());
		System.out.println(PropertiesUtil.getRouterDealerAddress());
	}

}
