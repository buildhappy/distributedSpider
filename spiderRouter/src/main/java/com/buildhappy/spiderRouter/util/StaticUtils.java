package com.buildhappy.spiderRouter.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.buildhappy.spiderRouter.model.StaticProModel;

public class StaticUtils {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StaticUtils.class);
	
	/**
	 * 清理没有心跳检测的工作模块
	 * @param taskQueue
	 */
	public static void purgeTimeoutStaticTask(Map<String , StaticProModel> taskQueue){
		//LOGGER.info("in purgeTimeoutTask(Map<String , StaticProTask> taskQueue");
		Set<String> keys = taskQueue.keySet();
		Iterator<String> it = keys.iterator();
		if(keys.size() <= 0){
			LOGGER.info("Purge task failed:No Task");
		}
		while(it.hasNext()){
			String key = it.next();
			StaticProModel task = taskQueue.get(key);
			if(task.getHeartbeat() < System.currentTimeMillis()){
				taskQueue.remove(key);
				LOGGER.info("Purge task(timeout):" + task);
				System.out.println("task:" + task.getHeartbeat());
				System.out.println("currentTimeMillis:" + System.currentTimeMillis());
			}
		}
	}
	/**
	 * 监听所有的工作模块
	 * @param taskQueue
	 */
	public static void monitorTask(Map<String , StaticProModel> taskQueue){
		//LOGGER.info("in monitorTask(Map<String , StaticProTask> taskQueue)");
		Set<String> keys = taskQueue.keySet();
		if(keys.size() <= 0){
			LOGGER.info("Monitor task failed:No Task");
		}
		Iterator<String> it = keys.iterator();
		int counter = 0;
		while(it.hasNext()){
			String key = it.next();
			StaticProModel task = taskQueue.get(key);
			LOGGER.info("Monitor task" + ++counter + ":"+ task);
		}
	}
	/**
	 * 重工作队列中取出负载最小的工作模块
	 * @param taskQueue
	 */
	public static StaticProModel getStaticModel(Map<String , StaticProModel> taskQueue){
		//TODO
		Set<String> keys = taskQueue.keySet();
		if(keys.size() <= 0){
			LOGGER.info("Get StaticModel failed:No Model");
			return null;
		}else{
			Iterator<String> it = keys.iterator();
			return taskQueue.get(it.next());
		}
		
	}
}
