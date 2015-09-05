package com.buildhappy.scheduler.imp;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import redis.clients.jedis.Jedis;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.utils.PropertiesUtil;
/**
 * Use the redis to store the done url and the BlockingQueue to store the todo url
 * @author buildhappy
 *
 */
public class RedisQueue{
	private final static int TO_DO_QUEUE_SIZE = 1000;
    private String redisIp = PropertiesUtil.getRedisServerIp();
    private int redisPort = PropertiesUtil.getRedisServerPort();
    private String siteRoot;
    
	private Jedis jedis = new Jedis(redisIp , redisPort);//非切片额客户端连接(单机连接)
	
	private BlockingQueue<Request> toDoQueue = new LinkedBlockingQueue<Request>(TO_DO_QUEUE_SIZE);
	
	public RedisQueue(String siteRoot){
		this.siteRoot = siteRoot;
	}
	
	public Request poll(Task task) {
		return null;
	}
	
	public void push(Request reqeust , Task task) {
		return;
	}
	
	public boolean isDoneRequest(Request request, Task task) {
    	String status = null;
    	status = jedis.hget(siteRoot, request.getUrl());
    	if(status != null && status.equals("1")){
    		return true;
    	}else{
    		return false;
    	}
	}
	
    public void storeToDoRequest(Request request , Task task){
    	System.out.println("storeToDoRequest(request , task) " + request.getUrl());
    	boolean isDoneRequest = isDoneRequest(request , null);
    	boolean isInToDoQueue = toDoQueue.contains(request);
    	if(!isDoneRequest && !isInToDoQueue && toDoQueue.size() < TO_DO_QUEUE_SIZE){
    		System.out.println("queue size:" + toDoQueue.size());
    		toDoQueue.add(request);//storeToDoRequestToQueue
    	}else if(!isDoneRequest && !isInToDoQueue && toDoQueue.size() >= TO_DO_QUEUE_SIZE){
    		//storeToDoRequestToRedis(request , task);
    	}
    }
    
    public void getToDoRequestFromRedis(Task task){
    	Set<String> keys = null;
    	int counter = 0;
    	keys = jedis.hkeys(siteRoot);
    	
    	for(String key : keys){
    		String status = jedis.hget(siteRoot, key);
    		//从redis中获取to do request,并将其设置为已处理
    		if(status != null && status.equals("0")){
    			toDoQueue.add(new Request(key));
    			jedis.hset(siteRoot , key, "1");
    			//将toDoQueue填满
    			if(++counter >= TO_DO_QUEUE_SIZE){
    				break;
    			}
    		}
    	}
    }
}
