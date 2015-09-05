package com.buildhappy;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.buildhappy.pipeline.DBPipeline;
import com.buildhappy.pipeline.Pipeline;
import com.buildhappy.utils.JsonUtils;
import com.buildhappy.utils.ZMQUtils;

/**
 * 基本可靠队列/简单的海盗模式
 * 当连续3次没有收到应答时，会关闭原来的链接重新建立链接
 * 发送心跳服务，接受页面处理的任务，启动静态页面处理模块进行工作
 *  how to use:
 *    cd /home/buildhappy/Documents/jzmq-master/src/main/perl
 *    javac HLClient.java
 *    java -Djava.library.path=/usr/local/lib -classpath ./ HLClient
 * @author buildhappy
 *
 */
public class StaticProClient{// implements Runnable
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StaticProClient.class);
	private final static int HEARTBEAT_INTERVAL = 1000;
	private final static int REQUEST_TIMEOUT = 2500;
	private final static int REQUEST_RETRIES = 3;
	private static String identity = "client1";
	private static final String StaticProClient_READY = "StaticProClient Ready";
	private static final String StaticProClient_HEARTBEAT = "StaticProClient Heartbeat";
	//private static ZContext ctx;
	//private static Socket client;
	private static final String startSpider = "start the Spider";
	private static Executor executor = Executors.newCachedThreadPool();
	private static int retriesLeft = REQUEST_RETRIES;
	
	private static final Socket dealerClient = ZMQUtils.getDealerSocket();
	private static final Socket subClient = ZMQUtils.getSUBSocket();
	private final static PollItem items[] = {new PollItem(dealerClient , ZMQ.Poller.POLLIN),
			new PollItem(subClient , ZMQ.Poller.POLLIN)};
	static{
		//load the spring context
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:springBeans.xml");//new FileSystemXmlApplicationContext("springBeans.xml");
	}
	public static void main(String[] args) {
		if(args.length >= 1){
			identity = args[0];
		}
		
		long heartbeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
		LOGGER.info("StaticProClient " + identity + " start");
		
		//向router发送准备就绪的信号
		String request;// = String.format("%d", ++sequence);
		try{
			//localIp = InetAddress.getByName("localhost").getHostAddress();//getLocalHost().getHostAddress();
			//request = String.format("%s", identity + "-" + StaticProClient_READY);
			ZMsg msg = new ZMsg();
			msg.addFirst(identity);
			msg.addFirst(StaticProClient_READY);//service type
			msg.addFirst(MDP.C_CLIENT.newFrame());
			msg.addFirst("");
			msg.send(dealerClient);
			msg.destroy();
		}catch(Exception e){
			e.printStackTrace();
		}

		//item.register(dealerClient , Poller.POLLIN);
		while(!Thread.currentThread().interrupted()){//retriesLeft > 0 && !Thread.currentThread().interrupted()
			//发送心跳检测信号
			if(System.currentTimeMillis() > heartbeatAt){
				heartbeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
				//String heartBeatReq = String.format("%s", identity + "-" + StaticProClient_HEARTBEAT);
				ZMsg msg = new ZMsg();
				msg.addFirst(identity);
				msg.addFirst(StaticProClient_HEARTBEAT);//service type
				msg.addFirst(MDP.C_CLIENT.newFrame());
				msg.addFirst("");
				msg.send(dealerClient);
				LOGGER.info("dealerClient:" + dealerClient);
			}
			int rc = ZMQ.poll(items, 2500);
			if(rc == -1){
				LOGGER.error("StaticProClient stop");
				break;
			}
			//get the reply message from workers(downloadModel or dynamicModel)
			LOGGER.error("dealerClient in StaticProClient:" + dealerClient);
			if(items[0].isReadable()){
				//if(items){
				LOGGER.error("receive reply msg from broker");
				ZMsg msg = ZMsg.recvMsg(dealerClient);
				String task = dealerClient.recvStr();
				msg.pop();
				String serviceType = msg.pop().toString();
				String message = msg.pop().toString();
				//[000] 
				//[014] downloaderTask(服务类型)
				//[012] downloadUrl-status-storagePath
				LOGGER.error("Receive reply from broker serviceName:" + msg.toString());
				LOGGER.error("Receive reply from broker message:" + task);
			}
			
			//System.out.println("rc:" + rc);
			if(items[1].isReadable()){
				String task = subClient.recvStr();
				LOGGER.info("Task from router:" + task);
				JsonUtils jsonUtil = new JsonUtils(task);
				String pagePro = jsonUtil.getPagePro();
				String taskType = jsonUtil.getTaskType();
				String triggerNode = jsonUtil.getTriggerNode();
				String siteRoot = jsonUtil.getSiteRoot();
				List<String> workerNode = jsonUtil.getWorkerNode();
				for(String worker:workerNode){
					System.out.println(worker);
				}
				//开始爬虫
				//消息格式:start the Spider-www.baidu.com-PageProBaidu
				if(taskType.equalsIgnoreCase(startSpider)){
			    	//StaticProSpider spider = null;// = new StaticProSpider(pagePro , siteRoot);
			    	LOGGER.info("Start spider. SiteRoot:" + siteRoot + ", pagePro:" + pagePro);
					if(triggerNode.equals(identity)){
				    	try {
				    		Pipeline pipeline = new DBPipeline();
				    		StaticProSpider spider = StaticProSpider.create(pagePro , siteRoot).addPipeline(pipeline).addUrl(siteRoot);
				    		executor.execute(spider);
				    		LOGGER.info("start spider trigger" + identity);
				    	} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						if(workerNode.contains(identity)){
					    	try {
					    		Thread.sleep(10000);
					    		Pipeline pipeline = new DBPipeline();
					    		StaticProSpider spider = StaticProSpider.create(pagePro , siteRoot).addPipeline(pipeline).addUrl(siteRoot);
					    		executor.execute(spider);
					    		LOGGER.info("start spider worker" + identity);
					    	} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
}
