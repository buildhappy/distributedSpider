package com.buildhappy.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import redis.clients.jedis.Jedis;

import com.buildhappy.Apk;
import com.buildhappy.MDP;
import com.buildhappy.ResultItems;
import com.buildhappy.Task;
import com.buildhappy.utils.PropertiesUtil;
import com.buildhappy.utils.ZMQUtils;
/**
 * 将获取的网页元素据存放到HBase数据库中,并且将下载任务发送到router
 * @author buildhappy
 *
 */
public class HBasePipeline implements Pipeline {
	private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static String redisIp = PropertiesUtil.getRedisServerIp();
    private static int redisPort = PropertiesUtil.getRedisServerPort();
    private Jedis jedis = new Jedis(redisIp , redisPort);
	@Autowired
	@Qualifier("ZMQUtils")
	private static ZMQUtils zmqUtil;
	//private static Socket dealerSocket;
	//MdClientApi clientSession = new MdClientApi(PropertiesUtil.getRouterDealerAddress());
	int counter = 0;
	public void process(ResultItems resultItems, Task task) {
		//Apk apk = resultItems.get("apk");
		Apk apk = resultItems.get("apk");
		String metaUrl = apk.getAppMetaUrl();
		String downloadUrl = apk.getAppDownloadUrl();
		System.out.println(apk);
		//TODO 将数据存到数据库中
		jedis.hset("spiederResult", apk.toString(), "0");
		//将待下载的任务发送给Downloader模块
		//String msg = "test";
		Socket dealerSocket = zmqUtil.getDealerSocket();
		ZMsg downloadRequest = new ZMsg();
		downloadRequest.addFirst(downloadUrl + "--" + counter++);
		downloadRequest.addFirst("downloader");
		downloadRequest.addFirst(MDP.C_CLIENT.newFrame());
		downloadRequest.addFirst("");
		downloadRequest.send(dealerSocket);
		LOGGER.info("dealerClient in HBasePipeline:" + dealerSocket);
		LOGGER.info("send downloader task to broker:\n" + downloadRequest.toString());
		
		ZMsg dynamicRequest = new ZMsg();
		dynamicRequest.addFirst("dynamicService--" + metaUrl + "--" + counter++);
		dynamicRequest.addFirst("dynamic");
		dynamicRequest.addFirst(MDP.C_CLIENT.newFrame());
		dynamicRequest.addFirst("");
		dynamicRequest.send(dealerSocket);
		LOGGER.info("dealerClient in HBasePipeline:" + dealerSocket);
		LOGGER.info("send dynamic task to broker:\n" + dynamicRequest.toString());
	}
	public static void main(String[] args){
		HBasePipeline pipeline = new HBasePipeline();
		pipeline.process(null, null);
		pipeline.process(null, null);
		pipeline.process(null, null);
	}
}
