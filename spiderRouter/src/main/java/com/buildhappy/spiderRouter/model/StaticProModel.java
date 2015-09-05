package com.buildhappy.spiderRouter.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 将静态页面处理模块的节点进行封装
 * @author buildhappy
 *
 */
public  class StaticProModel{
	private final static int HEARTBEAT_INTERVAL = 3000;
	private final static int HEARTBEAT_LIVENESS = 3;
	private final String routerAddress;
	//当前模块正在处理的任务数
	private AtomicInteger taskCounter;
	private long heartbeat;
	private String identity;

	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String ipAddress) {
		this.identity = ipAddress;
	}
	public StaticProModel(String routerAddress , String ipAddress){
		this(routerAddress);
		this.identity = ipAddress;
	}
	public StaticProModel(String routerAddress){
		this.routerAddress = routerAddress;
		this.taskCounter = new AtomicInteger(0);
		this.heartbeat = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
	}
	public int getTaskCounter() {
		return taskCounter.get();
	}
	public void setTaskCounter(int taskCounter) {
		this.taskCounter.set(taskCounter);
	}
	public int incrementTaskCounter(){
		return this.taskCounter.incrementAndGet();
	}
	public long getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}
	public String getRouterAddress(){
		return this.routerAddress;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(routerAddress);
		builder.append(" proforming ");
		builder.append(taskCounter.get());
		builder.append(" static page processor tasks");
		builder.append(" heartbeat " + heartbeat);
		return builder.toString();
	}
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof StaticProModel)){
			return false;
		}
		StaticProModel task = (StaticProModel) obj;
		return task.routerAddress.equals(this.routerAddress);
	}
	/*
	public int compareTo(Object o) {
		assert((o instanceof StaticProModel));
		StaticProModel obj = (StaticProModel)o;
		if(obj.taskCounter.get() > this.taskCounter.get())
			return 1;
		else if(obj.taskCounter.get() < this.taskCounter.get())
			return -1;
		return 0;
	}*/
}