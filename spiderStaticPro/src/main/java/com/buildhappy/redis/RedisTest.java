package com.buildhappy.redis;

import com.buildhappy.utils.PropertiesUtil;

import redis.clients.jedis.Jedis;

public class RedisTest {

	public static void main(String[] args) {		
		String siteRoot = "www.shouji.360.com" + "done";
	    String redisIp = PropertiesUtil.getRedisServerIp();
	    int redisPort = PropertiesUtil.getRedisServerPort();
	    Jedis jedis = new Jedis(redisIp , redisPort);
	    String status = jedis.hget(siteRoot, siteRoot + 22344);
	    System.out.println(status);
	    System.out.println(jedis.flushDB());
	}

}
