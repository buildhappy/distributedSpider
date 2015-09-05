package com.buildhappy.spiderRouter;
/**
 * 队列使用检测工人的信号扩展了负载均衡的模式。(P51)
 * 信号检测是那种看起来“简单”，但可能很难正确实现的。以后会改进
 * how to use:
 *    cd /home/buildhappy/Documents/jzmq-master/src/main/perl
 *    javac HLClient.java
 *    java -Djava.library.path=/usr/local/lib -classpath ./ HLClient
 * @author buildhappy
 */
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.buildhappy.spiderRouter.bean.StartSpiderMessage;
import com.buildhappy.spiderRouter.model.StaticProModel;
import com.buildhappy.spiderRouter.task.SpiderTask;
import com.buildhappy.spiderRouter.util.JsonUtils;
import com.buildhappy.spiderRouter.util.StaticUtils;

public class SpiderRouter_Old{
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpiderRouter_Old.class);
	private final static int HEARTBEAT_INTERVAL = 1000;
	private final static int HEARTBEAT_LIVENESS = 4;
	private static final String StaticProClient_READY = "StaticProClient Ready";
	private static final String StaticProClient_HEARTBEAT = "StaticProClient Heartbeat";
	private static final String StaticProClient_START = "start the Spider";
    protected final static int STAT_INIT = 0;
    protected final static int STAT_RUNNING = 1;
    protected final static int STAT_STOPPED = 2;
	//存放StaticProTask的队列
	private static final Map<String , StaticProModel> StaticModelQueue = new LinkedHashMap<String , StaticProModel>();
	//存放downloader的队列
	private static final Map<String , ?> DownlodModelQueue = null;
	
	private static final Map<String , SpiderTask> SpiderTaskQueue = new LinkedHashMap<String , SpiderTask>();
	
	/**
	 * 主要做三件事：接受前、后端的消息，定期向工人发送心跳包
	 * @param args
	 */
	public static void main(String[] args){
		LOGGER.info("SpiderRouter started");
		long heartbeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
		ZContext ctx = new ZContext();
		Socket staticSpiderRouter = ctx.createSocket(ZMQ.ROUTER);
		Socket backend = ctx.createSocket(ZMQ.ROUTER);
		Socket staticSpiderPub = ctx.createSocket(ZMQ.PUB);
		staticSpiderRouter.bind("tcp://127.0.0.1:5555");
		backend.bind("tcp://127.0.0.1:5556");
		staticSpiderPub.bind("tcp://127.0.0.1:5551");
		SpiderTaskQueue.put("www.shouji.360.com", new SpiderTask("www.shouji.360.com" , "PagePro360"));
		while(true){
			PollItem items[] = {new PollItem(backend , ZMQ.Poller.POLLIN),
								new PollItem(staticSpiderRouter , ZMQ.Poller.POLLIN)};
			int rc = ZMQ.poll(items, HEARTBEAT_INTERVAL);
			if(rc == -1){
				break;
			}
			
			//接受静态页面爬虫模块传递的消息,消息类型：
			//StaticProClient准备就绪格式：ip-StaticProClient_READY
			ZMsg msg = ZMsg.recvMsg(staticSpiderRouter , 1);//非阻塞式等待
			if(msg.size() > 1 && msg != null){
				ZFrame lastFrame = msg.getLast();
				String lastData = new String(lastFrame.getData());
				System.out.println("getLast:" + lastData);
				//System.out.println("ZMsg:" + msg.toString());//006B8B4567
				//msg.send(staticSpider);
				//准备就绪的消息
				if(lastData.contains(StaticProClient_READY)){
					String[] infos  = lastData.split("-");
					if(infos.length > 1){
						String client = infos[0];
						StaticProModel staticProTask = new StaticProModel(client);
						staticProTask.setTaskCounter(1);
						if(StaticModelQueue.get(client) == null){
							LOGGER.info("Put a new StaticTask:" + staticProTask);
							StaticModelQueue.put(client , staticProTask );
						}
					}
					//staticSpiderRouter.send("receive client ready message".getBytes(), 0);
				}//接受心跳检测信号
				else if(lastData.contains(StaticProClient_HEARTBEAT)){
					String[] infos  = lastData.split("-");
					if(infos.length > 1){
						String client = infos[0];
						StaticProModel staticProTask = StaticModelQueue.get(client);
						if(staticProTask != null){
							staticProTask.setHeartbeat(System.currentTimeMillis() + HEARTBEAT_INTERVAL * HEARTBEAT_LIVENESS);
						}
					}
				}else{
					//msg = ZMsg.recvMsg(staticSpiderRouter);
					System.out.println(msg.toString());
					//msg.push(worker);
					//msg.send(downloadWorker);
				}
				msg.destroy();
			}
			//定期清理静态页面爬虫的工作节点(超过规定时间没有心跳检测，就进行清理)
			if(System.currentTimeMillis() > heartbeatAt){
				StaticUtils.purgeTimeoutStaticTask(StaticModelQueue);
				StaticUtils.monitorTask(StaticModelQueue);
				heartbeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL * HEARTBEAT_LIVENESS;
			}
			staticSpiderPub.send("fafefwggge" , 0);
			//处理SpiderTask
			if(!SpiderTaskQueue.isEmpty()){
				Set<String> keys = SpiderTaskQueue.keySet();
				Iterator<String> it = keys.iterator();
				while(it.hasNext()){
					String key = it.next();
					SpiderTask spiderTask = SpiderTaskQueue.get(key);
					
					//StaticProModel spiderTask;
					if(spiderTask.getStatus() == STAT_INIT && !StaticModelQueue.isEmpty()){
						spiderTask.setStatus(STAT_RUNNING);
						//开始爬虫
						//消息格式(json):
						//[{"taskType":"Start Spider","pagePro":"pagePro360",
						//"siteRoot":"www.shouji.360.com","workerNode":["10.108.113.34","10.108.113.22"],
						//"triggerNode":"10.108.11.111"}]
						StartSpiderMessage message = new StartSpiderMessage();
						message.setPagePro(spiderTask.getPagePro());
						message.setSiteRoot(spiderTask.getSiteRoot());
						message.setTaskType(StaticProClient_START);
						message.setTriggerNode("127.0.0.1");
						message.setWorkerNode(new String[]{"10.108.113.34" , "10.108.113.22"});
						
						String sendData = JsonUtils.objectToJson(message);
						staticSpiderPub.send(sendData , 0);
						LOGGER.info("I:" + sendData);
					}
				}
			}
		}
		/*
		ArrayList<Worker> workers = new ArrayList<Worker>();
		//以常规时间间隔发出检测信号
		long heartbeat_at = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
		while(true){
			PollItem items[] = {new PollItem(backend , ZMQ.Poller.POLLIN),
							    new PollItem(frontend , ZMQ.Poller.POLLIN)};
			int rc = ZMQ.poll(items, workers.size() > 0 ? 2 : 1 , HEARTBEAT_INTERVAL);// , HEARTBEAT_INTERVAL  workers.size() > 0 ? 2 : 1
			//System.out.println("here");
			
			if(rc == -1){
				break;
			}
			//如果前端有消息传递过来，将前端的消息传递给后端
			if(items[0].isReadable()){
				ZMsg msg = ZMsg.recvMsg(backend);
				if(msg == null){
					break;
				}
				ZFrame address = msg.unwrap();
				Worker worker = new Worker(address);
				worker.ready(workers);
				//接受的是客户端的请求消息
				if(msg.size() == 1){
					ZFrame frame = msg.getFirst();
					String data = new String(frame.getData());
					System.out.println(data);
					if(!data.equals(PPP_READY) && !data.equals(PPP_HEARTBEAT)){
						System.out.println("E:invalid message from worker");
						msg.dump(System.out);
					}
					msg.destroy();
				}else{//接受的是工人的返回结果消息，将结果直接返回给前端
					msg.send(frontend);
				}
			}
			
			//接收后端的消息
			if(items[1].isReadable()){
				ZMsg msg = ZMsg.recvMsg(frontend);
				if(msg == null){
					break;
				}
				msg.push(Worker.next(workers));
				msg.send(backend);
			}
			//定期的向工人发送心跳信号
			if(System.currentTimeMillis() >= heartbeat_at){
				for(Worker worker:workers){
					worker.address.send(backend, ZFrame.REUSE + ZFrame.MORE);
					ZFrame frame = new ZFrame(PPP_HEARTBEAT);
					frame.send(backend, 0);
				}
				heartbeat_at = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
			}
			Worker.purge(workers);
			
		}
		while(workers.size() > 0){
			Worker worker = workers.remove(0);
		}
		
		
		workers.clear();
		*/
		ctx.destroy();
	}
}
