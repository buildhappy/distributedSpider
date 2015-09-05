package com.buildhappy.redis;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.utils.PropertiesUtil;

import redis.clients.jedis.Jedis;

public class RedisToolForSpider {
	protected Logger logger = LoggerFactory.getLogger(getClass());
    private static String redisIp = PropertiesUtil.getRedisServerIp();
    private static int redisPort = PropertiesUtil.getRedisServerPort();
    private final static int TO_DO_QUEUE_SIZE = PropertiesUtil.getToDoQueueSize();//init the ToDoQueue size TO_DO_QUEUE_SIZE
    private Jedis jedis = new Jedis(redisIp , redisPort);//非切片额客户端连接(单机连接)
    private final String siteRoot;
    protected BlockingQueue<Request> toDoQueue;//存放待抓取的url
    private final String siteRoot_Redis;
    public RedisToolForSpider(String siteRoot){
    	logger.debug("RedisToolForSpider constructor");
    	this.siteRoot = siteRoot;
    	this.toDoQueue = new LinkedBlockingQueue<Request>(TO_DO_QUEUE_SIZE);
    	siteRoot_Redis = siteRoot;
    }
    
    /**
     * get request from the job toDoQueue
     */
    public synchronized Request getToDoRequest(Task task){
    	logger.debug("RedisToolForSpider getToDoRequest(task) ");
    	Request request = null;
    	
    	if(toDoQueue.size() <= 0){
    		getToDoRequestFromRedis(null);
    	}
    	request = toDoQueue.poll();
    	if(request != null){
    		storeDoneRequest(request , task);
    	}
    	logger.debug("return from RedisToolForSpider getToDoRequest(): " + request);
    	return request;//Ordered to fetch
    }
    
    private void getToDoRequestFromRedis(Task task){
    	logger.debug("RedisToolForSpider getToDoRequestFromRedis()");
    	Set<String> keys = null;
    	int counter = 0;
    	keys = jedis.hkeys(siteRoot_Redis);
    	
    	for(String key : keys){
    		String status = jedis.hget(siteRoot_Redis, key);
    		//从redis中获取to do request,并将其设置为已处理
    		if(status != null && status.equals("1")){
    			toDoQueue.add(new Request(key));
    			jedis.hset(siteRoot_Redis, key, "0");
    			//将toDoQueue填满
    			if(++counter >= TO_DO_QUEUE_SIZE){
    				break;
    			}
    		}
    	}
    }
    
    /**
     * push request from the job toDoQueue
     * @param request
     */
    public synchronized void storeToDoRequest(Request request , Task task){
    	logger.debug("RedisToolForSpider storeToDoRequest " + request.getUrl());
    	boolean isDoneRequest = isDoneRequest(request , null);
    	boolean isInToDoQueue = toDoQueue.contains(request);
    	//System.out.println("isDoneRequest:" + isDoneRequest + " isInToDoQueue:" + isInToDoQueue);
    	if(!isDoneRequest && !isInToDoQueue && toDoQueue.size() < TO_DO_QUEUE_SIZE){
    		//System.out.println("queue size:" + toDoQueue.size());
    		toDoQueue.add(request);//storeToDoRequestToQueue
    	}else if(!isDoneRequest && !isInToDoQueue && toDoQueue.size() >= TO_DO_QUEUE_SIZE){
    		storeToDoRequestToRedis(request , task);
    	}
    }
    /**
     * push some todo requet to the redis cache
     */
    public void storeToDoRequestToRedis(Request request , Task task){
    	logger.debug("RedisToolForSpider storeToDoRequestToRedis()");
    	jedis.hset(siteRoot, request.getUrl(), "1");
    }
    
    /**
     * push done requet to the redis cache
     */
    public synchronized void storeDoneRequest(Request request, Task task){
    	logger.debug("RedisToolForSpider storeDoneRequest()");
    	if(isDoneRequest(request , task) == false){
    		jedis.hset(siteRoot_Redis, request.getUrl(), "0");//1为未处理，0为已处理完，2为正在处理,3处理失败
    	}
    }
    
    /**
     * is the request contains in the job done queue
     */
    private boolean isDoneRequest(Request request , Task task){
    	logger.debug("RedisToolForSpider isDoneRequest()");
    	boolean flag = false;
    	//return doneQueue.contains(request);
    	String status = null;
    	//System.out.println(siteRoot + ":" + request.getUrl());
    	status = jedis.hget(siteRoot_Redis, request.getUrl());
    	if(status != null && status.equals("0")){
    		flag = true;
    	}else{
    		flag =  false;
    	}
    	logger.debug(request.getUrl() +  " isDoneRequest():" + flag);
    	return flag;
    	
    }
}
