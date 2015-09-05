package com.buildhappy.spiderRouter.task;

import java.util.HashSet;
import java.util.Set;

/**
 * 对爬虫任务进行封装
 * @author buildhappy
 *
 */
public class SpiderTask {
	//爬虫起始的url
	private final String siteRoot;
	//页面处理类
	private final String pagePro;
	private Status status;
    protected final static int STAT_INIT = 0;
    protected final static int STAT_RUNNING = 1;
    protected final static int STAT_STOPPED = 2;
    private String triggerNode;//address of this Spider Model
    private int workerNum;//the number of workers working for the task(including worker node and trigger node) 

	private Set<String> workers;
    
	public String getTriggerNode() {
		return triggerNode;
	}
	//if this work node is working for this task 
	public boolean ifThisWorker(String worker){
		if(worker.equals(triggerNode)){
			return true;
		}
		return workers.contains(worker);
	}
	public void setTriggerNode(String triggerNode) {
		if(triggerNode != null && triggerNode.trim().length() >= 1){
			this.triggerNode = triggerNode;
			workerNum++;
		}
		
	}
	public void addWorker(String worker){
		if(workers.add(worker)){
			workerNum++;
		}
	}
	public SpiderTask(String siteRoot , String pagePro){
		this.siteRoot = siteRoot;
		this.pagePro = pagePro;
		status = Status.fromValue(STAT_INIT);
		this.workers = new HashSet<String>();
		this.workerNum = 0;
	}
	
	public void setStatus(int value){
		this.status = Status.fromValue(value);
	}
	public int getStatus(){
		return this.status.value;
	}
	public String getSiteRoot(){
		return this.siteRoot;
	}
	public String getPagePro(){
		return this.pagePro;
	}
    public int getWorkerNum() {
		return workerNum;
	}
	public enum Status{
		Init(0) , Running(1) , Done(2) , Failed(3);
		private Status(int value){
			this.value = value;
		}
		private int value;
		int getValue(){
			return value;
		}
		public static Status fromValue(int value){
			for(Status status : Status.values()){
				if(status.value == value){
					return status;
				}
			}
			return Init;
		}
	}
}
