package com.buildhappy.spiderRouter.bean;

public class StartSpiderMessage {
	//任务类型，eg.Start Spider
	private String taskType;
	//首先启动的结点，即刻启动，获取的url存放到redis中，供workerNode使用
	private String TriggerNode;
	//其他的工作节点，稍后启动
	private String[] workerNode;
	//待爬取的网站
	private String siteRoot;
	//该网站的页面处理类
	private String pagePro;
	
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	public String getTriggerNode() {
		return TriggerNode;
	}
	public void setTriggerNode(String triggerNode) {
		TriggerNode = triggerNode;
	}
	public String[] getWorkerNode() {
		return workerNode;
	}
	public void setWorkerNode(String[] workerNode) {
		this.workerNode = workerNode;
	}
	public String getSiteRoot() {
		return siteRoot;
	}
	public void setSiteRoot(String siteRoot) {
		this.siteRoot = siteRoot;
	}
	public String getPagePro() {
		return pagePro;
	}
	public void setPagePro(String pagePro) {
		this.pagePro = pagePro;
	}
	
	
}
