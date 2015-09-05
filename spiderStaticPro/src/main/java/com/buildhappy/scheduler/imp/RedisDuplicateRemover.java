package com.buildhappy.scheduler.imp;

import redis.clients.jedis.Jedis;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.scheduler.DuplicateRemover;

/**
 * 参照HashSetDuplicateRemover,将已经处理的url放在redis中
 * @author buildhappy
 *
 */
public class RedisDuplicateRemover implements DuplicateRemover{
    private String redisIp = "127.0.0.1";
    private int redisPort = 6379;
    private String siteRoot;
    
	private Jedis jedis = new Jedis(redisIp , redisPort);//非切片额客户端连接(单机连接)
	
	public boolean isDuplicate(Request request, Task task) {
    	String status = null;
    	status = jedis.hget(siteRoot, request.getUrl());
    	if(status != null && status.equals("1")){
    		return true;
    	}else{
    		return false;
    	}
	}

	public void resetDuplicateCheck(Task task) {
		
	}

	public int getTotalRequestsCount(Task task) {
		
		return 0;
	}

}
