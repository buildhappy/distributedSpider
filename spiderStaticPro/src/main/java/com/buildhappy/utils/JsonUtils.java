package com.buildhappy.utils;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * get data from jsonData
 * @author buildhappy
 *
 */
public class JsonUtils {
	private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	private final String jsonData;
	public JsonUtils(String jsonData){
		this.jsonData = jsonData;
	}
	
	public String getDataFromJson(String key){
		JSONArray obj = JSONArray.fromObject(jsonData);
		JSONObject jsonObj = (JSONObject)obj.get(0);
		return (String) jsonObj.get(key);
	}
	
	public String getTaskType(){
		return getDataFromJson("taskType");
	}
	
	public String getSiteRoot(){
		return getDataFromJson("siteRoot");
	}
	
	public String getPagePro(){
		return getDataFromJson("pagePro");
	}
	
	public List getWorkerNode(){
		JSONArray obj = JSONArray.fromObject(jsonData);
		JSONObject jsonObj = (JSONObject)obj.get(0);
		JSONArray jsonArray = jsonObj.getJSONArray("workerNode");
		return JSONArray.toList(jsonArray);
		//return (String[]) jsonObj.get("workerNode");
	}
	
	public String getTriggerNode(){
		return getDataFromJson("triggerNode");
	}
	
	public static void main(String[] args){
		/*
		Queue<Apk> list = new LinkedList<Apk>();
		Apk user = new Apk("222" , "343",null,null,null);
		list.add(user);
		//logger.info(JsonUtils.convertListToString(list));
		System.out.println(getSenderData("0" , "ch2" , "" ,"", list));
		*/
		
		/*
		StartSpiderMessage message = new StartSpiderMessage();
		message.setPagePro("pagePro360");
		message.setSiteRoot("www.shouji.360.com");
		message.setTaskType("Start Spider");
		message.setTriggerNode("10.108.11.111");
		message.setWorkerNode(new String[]{"10.108.113.34" , "10.108.113.22"});
		String jsonData = objectToJson(message);
		System.out.println(jsonData);
		
		JSONArray obj = JSONArray.fromObject(jsonData);

		JSONObject jsonObj = (JSONObject)obj.get(0);
		System.out.println(jsonObj.get("taskType"));
		System.out.println(jsonObj.get("workerNode"));
		*/
	}
}
