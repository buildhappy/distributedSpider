package com.buildhappy.scheduler.imp;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.scheduler.DuplicateRemovedScheduler;

/**
 * 
 * @author buildhappy
 *
 */
public class RedisScheduler extends DuplicateRemovedScheduler{
	private String siteRoot;
	RedisQueue redisQueue = new RedisQueue(siteRoot);
	public Request poll(Task task) {
		// TODO Auto-generated method stub
		return null;
	}

}
