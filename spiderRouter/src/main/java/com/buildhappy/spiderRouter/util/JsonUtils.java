package com.buildhappy.spiderRouter.util;

import java.util.Map;
import java.util.Queue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buildhappy.spiderRouter.bean.StartSpiderMessage;

/**
 * utils for converting Object to json data
 * @author buildhappy
 *
 */
public class JsonUtils {
	private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
	/**
	 * convert List data to json style
	 * @param list
	 * @return
	 */
	public static String listToJson(Queue list){
		logger.info("JsonUtils listToJson parameter:Queue");
		JSONArray jsonArray = JSONArray.fromObject(list);
		logger.info("JsonUtils listToJson return:json data");
		return jsonArray.toString();
	}
	
	/**
	 * convert Object data to json style
	 * @param o
	 * @return
	 */
	public static String objectToJson(Object o){
		logger.info("JsonUtils objectToJson parameter:Object");
		JSONArray jsonObj = JSONArray.fromObject(o);
		logger.info("JsonUtils objectToJson return:json data");
		return jsonObj.toString();
	}
	
	/**
	 * convert map data to json style
	 * @param map
	 * @return
	 */
	public static String mapToJson(Map<String, Object> map){
		logger.info("JsonUtils mapToJson parameter:Map");
		JSONObject jsonArray = JSONObject.fromObject(map);
		logger.info("JsonUtils mapToJson return:json data");
		return jsonArray.toString();
	}
	
	/**
	 * generate the data in a certain format
	 * @param agentId
	 * @param channelId
	 * @param header
	 * @param status
	 * @param apps
	 * @return
	    agentId : agent_id
        channelId : channel_id
        header : header
        status : status // status 可能是running=3，done=4，也可能是failed=0
        details : {
            // 一个渠道中有n个应用，n=0
            appName : app_name
            appVersion : app_version
            appVenderName : app_vender_name
            appPackageName : app_package_name
            appType : app_type
            appSize : app_size
            appTsChannel : app_ts_channel // app上传时间
            osPlatform : os_platform // 适合的操作系统
            appDetailsUrl : app_details_url
            cookie : cookie // 一个应用一个cookie
            appDownloadUrl : app_download_url
        }
	 
	public static String getSenderData(String agentId , String channelId, String header,String status , Queue<Apk> apps){
		//String data = "{\"agentId\":\"" + agentId + "\",\"channelId\":\"" + channelId + "\",";
		logger.info("JsonUtils getSenderData parameter:agentId,channelId,status,apps");
		agentId = "eb62f8b3cc39c06a5654";
		String data = "{\"agentId\":\"" + agentId + "\",\"channelId\":\"" + channelId + "\"," + "\"header\":\"" + header + "\"," +"\"status\":\"" + status + "\"," + "\"details\":";
		String appsData = listToJson(apps);
		data = data + appsData + "}";
		logger.info("JsonUtils getSenderData return:json data");
		return data;
	}
	*/
	
	public static String getSenderData(String agentId , String channelId, String header,String status , String apps){
		//String data = "{\"agentId\":\"" + agentId + "\",\"channelId\":\"" + channelId + "\",";
		logger.info("JsonUtils getSenderData parameter:agentId,channelId,status,apps");
		agentId = "eb62f8b3cc39c06a5654";
		String data = "{\"agentId\":\"" + agentId + "\",\"channelId\":\"" + channelId + "\"," + "\"header\":\"" + header + "\"," +"\"status\":\"" + status + "\"," + "\"details\":";
		//String appsData = apps;
		data = data + apps + "}";
		logger.info("JsonUtils getSenderData return:json data");
		return data;
	}
	
	public static void main(String[] args){
		/*
		Queue<Apk> list = new LinkedList<Apk>();
		Apk user = new Apk("222" , "343",null,null,null);
		list.add(user);
		//logger.info(JsonUtils.convertListToString(list));
		System.out.println(getSenderData("0" , "ch2" , "" ,"", list));
		*/
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
		
	}
}
