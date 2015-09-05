package com.buildhappy.redis;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.buildhappy.utils.PropertiesUtil;

/**
 * 查看爬虫结果
 * @author buildhappy
 *
 */
public class GetSpiderResult {
	private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static String redisIp = PropertiesUtil.getRedisServerIp();
    private static int redisPort = PropertiesUtil.getRedisServerPort();
    private static Jedis jedis = new Jedis(redisIp , redisPort);
    private static String siteRoot = "http://zhushou.360.cn/";
    private static String resultKey = "spiederResult";
    
	public static void main(String[] args) {
		getResultNum();
		getResultDetail();
	}
	public static Set<String> spiderKeySet(){
		Set<String> keys = jedis.hkeys(resultKey);
		return keys;
	}
	public static void getResultNum(){
		Set<String> keys = spiderKeySet();
		System.out.println("爬虫共获取的应用个数:\n" + keys.size());
	}
	public static void getResultDetail(){
		Set<String> keys = spiderKeySet();
		System.out.println("爬虫获取的应用详情:\n");
		for(String key:keys)
			System.out.println(key);
	}
}
