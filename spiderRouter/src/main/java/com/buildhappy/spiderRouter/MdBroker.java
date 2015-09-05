package com.buildhappy.spiderRouter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.buildhappy.spiderRouter.bean.StartSpiderMessage;
import com.buildhappy.spiderRouter.mdp.MDP;
import com.buildhappy.spiderRouter.model.StaticProModel;
import com.buildhappy.spiderRouter.task.SpiderTask;
import com.buildhappy.spiderRouter.util.JsonUtils;
import com.buildhappy.spiderRouter.util.SpiderModelGetter;
/**
 *  how to use:
 *   cd /home/buildhappy/Documents/jzmq-master/src/main/perl
 *   javac HLClient.java
 *   java -Djava.library.path=/usr/local/lib -classpath ./ HLClient
 * @author buildhappy
 *
 */
public class MdBroker {
	private static final String INTERVAL_SERVICE_PREFIX = "mmi";
	private static final int HEARTBEAT_LIVENESS = 3;//3-5都可以
	private static final int HEARTBEAT_INTERVAL = 25000;
	private static final int HEARTBEAT_EXPIRY = HEARTBEAT_LIVENESS * HEARTBEAT_INTERVAL;
	private static final String StaticProClient_READY = "StaticProClient Ready";
	private static final String StaticProClient_HEARTBEAT = "StaticProClient Heartbeat";
	private static final String StaticProClient_START = "start the Spider";
	private static final String StaticProClient_DOWNLOAD = "StaticProClient DownLoad";
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MdBroker.class);
    protected final static int STAT_INIT = 0;
    protected final static int STAT_RUNNING = 1;
    protected final static int STAT_STOPPED = 2;
    private float factor = 1;

    //存放StaticProTask的队列
	private static final Map<String , StaticProModel> StaticModels = new LinkedHashMap<String , StaticProModel>();
	private static final BlockingQueue<StaticProModel> StaticModelQueue = new LinkedBlockingQueue<StaticProModel>();
	//存放downloader的队列
	private static final Map<String , ?> DownlodModelQueue = null;
	//爬取的任务
	private static final Map<String , SpiderTask> SpiderTaskQueue = new LinkedHashMap<String , SpiderTask>();
	
	/**
	 * 比如coffee,tea
	 * @author buildhappy
	 */
	private static class Service{
		public final String name;
		Deque<ZMsg> requests;//客户端的请求
		Deque<Worker> waiting;//等待的工作者
		public Service(String name){
			this.name = name;
			this.requests = new ArrayDeque<ZMsg>();
			this.waiting = new ArrayDeque<Worker>();
		}
	}
	
	private static class Worker{
		String identity;
		ZFrame address;
		Service service;//所属的service
		long expiry;
		
		public Worker(String identity , ZFrame address){
			this.address = address;
			this.identity = identity;
			this.expiry = System.currentTimeMillis() + HEARTBEAT_LIVENESS * HEARTBEAT_INTERVAL;
		}
	}
	
	private ZContext ctx;
	private Socket dealerSocket;
	private Socket staticSpiderPub;
	
	private long heartbeatAt;
	private Map<String , Service> services;
	private Map<String , Worker> workers;
	private Deque<Worker> idleWorkers;//the workers waiting for task
	private boolean verbose = false;
	//private Formatter log = new Formatter(System.out);
	
	public MdBroker(boolean verbose){
		LOGGER.info("MdBroker constructor");
		this.verbose = verbose;
		this.services = new HashMap<String , Service>();
		this.workers = new HashMap<String , Worker>();
		this.idleWorkers = new ArrayDeque<Worker>();
		this.heartbeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
		this.ctx = new ZContext();
		this.dealerSocket = ctx.createSocket(ZMQ.ROUTER);
		this.staticSpiderPub = ctx.createSocket(ZMQ.PUB);
	}
	
	/**
	 * 主要的工作在此函数中完成
	 */
	public void mediate(){
		LOGGER.info("MdBroker mediate()");
		while(!Thread.currentThread().isInterrupted()){
			Poller items = new Poller(1);
			items.register(dealerSocket , Poller.POLLIN);
			if(items.poll(HEARTBEAT_INTERVAL) == -1){
				break;
			}
			if(items.pollin(0)){
				ZMsg msg = ZMsg.recvMsg(dealerSocket);
				//System.out.println(msg);
				if(msg == null)
					break;
				if(verbose){
					LOGGER.info("I:broker received message \n");
					LOGGER.info(msg.toString());
				}
				
				//the msg that router received
				//[005] 006B8B4567	Router自动添加身份标识
				//[000] 
				//[006] MDPC01
				//[004] echo
				//[011] Hello world
				//msg.dump(log.out());
				ZFrame sender = msg.pop();//
				ZFrame empty = msg.pop();
				ZFrame header = msg.pop();//the type of sender,eg.MDPC01,MDW01..
				if(MDP.C_CLIENT.frameEquals(header)){
					//System.out.println("C_CLIENT");
					processClient(sender , msg);
				}else if(MDP.DOWNLOAD_WORKER.frameEquals(header)){
					//System.out.println("W_WORKER");
					processDownloadWorker(sender , msg);
				}
				else if(MDP.DYNAMIC_WORKER.frameEquals(header)){
					//TODO dynamic page processor
					processDynamicWorker(sender , msg);
				}else{
					LOGGER.error("E:invallid messge");
					LOGGER.error(msg.toString());
					msg.destroy();
				}
				sender.destroy();
				empty.destroy();
				header.destroy();
			}
			purgeWorkers();
			sendHeartbeats();
			sendSpiderTask();//dispatch crawler url to clients
		}
		destroy();
	}
	/**
	 * disconnect all workers , destroy context
	 */
	private void destroy(){
		LOGGER.info("MdBroker destroy()");
		Worker[] deleteList = workers.entrySet().toArray(new Worker[0]);
		for(Worker worker : deleteList){
			deleteWorker(worker , true);
		}
		ctx.destroy();
	}
	//the ZMsg msg contains the following message:
	//[004] echo
	//[011] Hello world
	
	//the ZFrame sender is:[005] 006B8B4567
	public void processClient(ZFrame sender , ZMsg msg){
		LOGGER.info("MdBroker processClient()");
		assert(msg.size() >= 2);//service name + body
		ZFrame serviceFrame = msg.pop();
		msg.wrap(sender.duplicate());
		String service = serviceFrame.toString();
		//System.out.println(serviceFrame.toString());
		/*
		if(serviceFrame.toString().startsWith(INTERVAL_SERVICE_PREFIX)){//not run here
			System.out.println("fagfagergewgbgtswet");
			serviceInternal(serviceFrame , msg);
		}else
			dispatch(requireService(serviceFrame) , msg);
		*/
		
		//StaticProClient Ready
		//Messge tpye：ip-StaticProClient_READY
		if(service.contains(StaticProClient_READY)){
			String client = sender.strhex();
			msg.pop();
			msg.pop();
			String identity = msg.popString();
			StaticProModel staticProTask = new StaticProModel(client , identity);

			staticProTask.setTaskCounter(1);
			/*
			 if(StaticModelQueue.contains(client)){
				LOGGER.info("Put a new StaticTask:" + staticProTask);
				//StaticModels.put(client , staticProTask );
				StaticModelQueue.offer(staticProTask);
			}
			*/
			if(StaticModels.get(client) == null){
				LOGGER.info("Put a new StaticTask:" + staticProTask);
				StaticModels.put(client , staticProTask );
			}
			
		}
		//StaticProClient Heartbeat
		//Messge tpye：ip-StaticProClient Heartbeat
		else if(service.contains(StaticProClient_HEARTBEAT)){
			String client = sender.strhex();
			//StaticModelQueue//
			/**/
			StaticProModel staticProTask = StaticModels.get(client);
			
			if(staticProTask != null){
				staticProTask.setHeartbeat(System.currentTimeMillis() + HEARTBEAT_INTERVAL * HEARTBEAT_LIVENESS);
			}
		}
		else{// if(service.contains(StaticProClient_DOWNLOAD)){
			dispatch(requireService(serviceFrame) , msg);
		}
		serviceFrame.destroy();
	}
	/**
	 * 处理由工人发送给代理的就绪、应答、信号检查，或断开链接(ready,reply,heartbeat,disconnect)消息
	 * @param sender:the identity of worker eg.006B8B4567
	 * @param msg:the command and service type eg.W_READY,echo
	 */
	private void processDownloadWorker(ZFrame sender , ZMsg msg){
		LOGGER.info("MdBroker processDownloadWorker()");
		assert(msg.size() >= 1);
		ZFrame command = msg.pop();
		//sender.strhex():returns frame data as a printable hex string
		boolean workerReady = workers.containsKey(sender.strhex());
		Worker worker = requireWorker(sender);//get worker from workers(a Map container) according to identity
		if(MDP.W_READY.frameEquals(command)){
			if(workerReady || sender.toString().startsWith(INTERVAL_SERVICE_PREFIX)){
				deleteWorker(worker , true);
			}else{
				ZFrame serviceFrame = msg.pop();
				worker.service = requireService(serviceFrame);//locate the service from services(a Map container) (create if necessary)
				workerWaiting(worker);
				serviceFrame.destroy();
			}
		}else if(MDP.W_REPLY.frameEquals(command)){
			/**
			 * 	ZFrame client = msg.unwrap();
				msg.addFirst(worker.service.name);
				msg.addFirst(MDP.C_CLIENT.newFrame());
				msg.wrap(client);
				msg.send(socket);
			 */
			//System.out.println("receive msg from worker:" + msg.toString());
			if(workerReady){
				ZFrame client = msg.unwrap();
				msg.addFirst(worker.service.name);
				msg.addFirst(MDP.C_CLIENT.newFrame());
				//msg.addFirst("");
				//msg.addFirst(client);
				msg.wrap(client);
				msg.send(dealerSocket);
				LOGGER.info("send reply msg to client(dynamic worker):" + dealerSocket.send(msg.toString(), 0));
				LOGGER.info("send reply msg to client(dynamic worker):" + client.toString()+ "\n" + msg.toString());
				workerWaiting(worker);
			}else{
				deleteWorker(worker , true);
			}
		}else if(MDP.W_HEARTBEAT.frameEquals(command)){
			if(workerReady){
				worker.expiry = System.currentTimeMillis() + HEARTBEAT_EXPIRY;
			}else{
				deleteWorker(worker , true);
			}
		}else if(MDP.W_DISCONNECT.frameEquals(command)){
			deleteWorker(worker , true);
		}else{
			LOGGER.info("E:invalid messge");
			LOGGER.info(msg.toString());
		}
		msg.destroy();
	}
	/**
	 * send message to worker.
	 * If message is provided,sends that message.
	 * Does not destroy the message,this is the caller's job
	 * @param worker
	 * @param command
	 * @param option
	 * @param msgp
	 */
	private void sendToWorker(Worker worker , MDP command , String option , ZMsg msgp){
		LOGGER.info("MdBroker sendToWorker()");
		ZMsg msg = msgp == null ? new ZMsg() : msgp.duplicate();
		if(option != null)
			msg.addFirst(new ZFrame(option));
		msg.addFirst(command.newFrame());
		msg.addFirst(MDP.DOWNLOAD_WORKER.newFrame());
		//stack routing envelope to start of message
		msg.wrap(worker.address.duplicate());
		if(verbose){
			LOGGER.info("I:sending " + command + " to worker\n");
			LOGGER.info(msg.toString());
		}
		msg.send(dealerSocket);
		LOGGER.info("MdBroker sendToWorker()\n" + msg.toString());
	}
	private void processDynamicWorker(ZFrame sender , ZMsg msg){
		LOGGER.info("MdBroker processDynamicWorker()");
		assert(msg.size() >= 1);
		ZFrame command = msg.pop();
		//sender.strhex():returns frame data as a printable hex string
		boolean workerReady = workers.containsKey(sender.strhex());
		Worker worker = requireWorker(sender);//get worker from workers(a Map container) according to identity
		if(MDP.W_READY.frameEquals(command)){
			if(workerReady || sender.toString().startsWith(INTERVAL_SERVICE_PREFIX)){
				deleteWorker(worker , true);
			}else{
				ZFrame serviceFrame = msg.pop();
				worker.service = requireService(serviceFrame);//locate the service from services(a Map container) (create if necessary)
				workerWaiting(worker);
				serviceFrame.destroy();
			}
		}else if(MDP.W_REPLY.frameEquals(command)){
			/**
			 * 	ZFrame client = msg.unwrap();
				msg.addFirst(worker.service.name);
				msg.addFirst(MDP.C_CLIENT.newFrame());
				msg.wrap(client);
				msg.send(socket);
			 */
			//System.out.println("receive msg from worker:" + msg.toString());
			if(workerReady){
				ZFrame client = msg.unwrap();
				msg.addFirst(worker.service.name);
				msg.addFirst(MDP.C_CLIENT.newFrame());
				//msg.addFirst("");
				//msg.addFirst(client);
				msg.wrap(client);
				msg.send(dealerSocket);
				LOGGER.info("send reply msg to client(dynamic worker):" + dealerSocket.send(msg.toString(), 0));
				LOGGER.info("send reply msg to client(dynamic worker):" + client.toString()+ "\n" + msg.toString());
				workerWaiting(worker);
			}else{
				deleteWorker(worker , true);
			}
		}else if(MDP.W_HEARTBEAT.frameEquals(command)){
			if(workerReady){
				worker.expiry = System.currentTimeMillis() + HEARTBEAT_EXPIRY;
			}else{
				deleteWorker(worker , true);
			}
		}else if(MDP.W_DISCONNECT.frameEquals(command)){
			deleteWorker(worker , true);
		}else{
			LOGGER.info("E:invalid messge");
			LOGGER.info(msg.toString());
		}
		msg.destroy();
	}
	/**
	 * delete worker form all data structures , and destroy worker
	 * @param worker
	 * @param disconnect
	 */
	private void deleteWorker(Worker worker , boolean disconnect){
		LOGGER.info("MdBroker deleteWorker()");
		assert(worker != null);
		if(disconnect){
			sendToWorker(worker , MDP.W_DISCONNECT , null , null);
		}
		if(worker.service != null){
			worker.service.waiting.remove(worker);
		}
		workers.remove(worker.identity);
		worker.address.destroy();
	}
	/**
	 * finds the worker(create if nessary)
	 * @param address
	 * @return
	 */
	private Worker requireWorker(ZFrame address){
		LOGGER.info("MdBroker requireWorker()");
		assert(address != null);
		String identity = address.strhex();//created by router
		//System.out.println("worker identity:" + identity);
		Worker worker = workers.get(identity);
		if(worker == null){
			worker = new Worker(identity , address.duplicate());
			workers.put(identity, worker);
			if(verbose){
				LOGGER.info("I:registering new worker.\n" + identity);
			}
		}
		return worker;
	}
	/**
	 * locate the service from services(a Map container) (create if necessary)
	 * @param serviceFrame
	 * @return
	 */
	private Service requireService(ZFrame serviceFrame){
		LOGGER.info("MdBroker requireService()");
		assert(serviceFrame != null);
		String name = serviceFrame.toString();
		Service service = services.get(name);
		if(service == null){
			service = new Service(name);
			services.put(name,  service);
		}
		return service;
	}
	/**
	 * bind broker to endpoint,can call this mulitiple times
	 * we use a single socket for both clients and workers 
	 * @param endpoit
	 */
	private void bind(String endpoint , String pubPoint){
		LOGGER.info("MdBroker bind()");
		dealerSocket.bind(endpoint);
		staticSpiderPub.bind(pubPoint);;
		LOGGER.info("MDP broker is active at " + endpoint);
	}
	/**
	 * handle interval service according to 8/MMI specification
	 * 将client的消息转发给worker
	 * MMI:管家的管理接口
	 * @param serviceFrame
	 * @param msg
	 */
	private void serviceInternal(ZFrame serviceFrame , ZMsg msg){
		LOGGER.info("MdBroker serviceInternal()");
		String returnCode = "501";
		if("mmi.service".equals(serviceFrame.toString())){
			String name = msg.peekLast().toString();
			returnCode = services.containsKey(name) ? "200" : "400";
		}
		msg.peekLast().reset(returnCode.getBytes());
		ZFrame client = msg.unwrap();
		msg.addFirst(serviceFrame.duplicate());
		msg.addFirst(MDP.C_CLIENT.newFrame());
		msg.wrap(client);
		msg.send(dealerSocket);
	}
	
	/**
	 * send heartbeat to idle workers if it's time
	 */
	public synchronized void sendHeartbeats(){
		LOGGER.info("MdBroker sendHeartbeats()");
		if(System.currentTimeMillis() >= heartbeatAt){
			for(Worker worker : idleWorkers){
				sendToWorker(worker , MDP.W_HEARTBEAT , null , null);
			}
			heartbeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
		}
	}
	/**
	 * look for & kill expired workers.Workers are oldest to most recent
	 * we stop at the first alive worker
	 */
	public synchronized void purgeWorkers(){
		LOGGER.info("MdBroker purgeWorkers()");
		Iterator<Worker> iterator = idleWorkers.iterator();
		while(iterator.hasNext()){
			Worker w = iterator.next();
			if(w.expiry < System.currentTimeMillis()){
				iterator.remove();
				LOGGER.info("I:deleting expired worker.%s" + w.identity);
				deleteWorker(w , false);
			}
		}
	}
	
	/**
	 * this worker is now waiting for work
	 * @param worker
	 */
	public synchronized void workerWaiting(Worker worker){
		LOGGER.info("MdBroker workerWaiting()");
		idleWorkers.addLast(worker);
		worker.service.waiting.addLast(worker);
		worker.expiry = System.currentTimeMillis() + HEARTBEAT_EXPIRY;
		dispatch(worker.service , null);
	}
	/**
	 * dispatch requests to waiting workers as possible
	 * @param service
	 * @param msg
	 */
	private void dispatch(Service service , ZMsg msg){
		LOGGER.info("MdBroker dispatch()");
		assert(service != null);
		if(msg != null)
			service.requests.offerLast(msg);
		purgeWorkers();
		System.out.println("service.name:" + service.name + " request:" + service.requests.size());
		while(!service.waiting.isEmpty() && !service.requests.isEmpty()){
			msg = service.requests.pop();
			Worker worker = service.waiting.pop();
			idleWorkers.remove(worker);
			sendToWorker(worker , MDP.W_REQUEST , null , msg);
			msg.destroy();
		}
	}
	
	public void sendSpiderTask(){
		LOGGER.info("MdBroker sendSpiderTask()");
		//dispatch SpiderTask to StaticModel(client)
		if(!SpiderTaskQueue.isEmpty() && !StaticModels.isEmpty()){
			Set<String> keys = SpiderTaskQueue.keySet();
			Iterator<String> it = keys.iterator();
			while(it.hasNext()){
				String key = it.next();
				SpiderTask spiderTask = SpiderTaskQueue.get(key);
				
				//StaticProModel spiderTask;
				if(spiderTask.getStatus() == STAT_INIT || 
					spiderTask.getWorkerNum() < (int)(StaticModels.size() * factor)){
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
					
					SpiderModelGetter spiderGetter = new SpiderModelGetter(StaticModels);
					String triggerNode = spiderGetter.getTriggerNode();
					message.setTriggerNode(triggerNode);
					spiderTask.setTriggerNode(triggerNode);
					String[] workNodes = spiderGetter.getWorkerNodes();
					String[] workers = new String[workNodes.length];
					/**/
					for(int i = 0; i < workNodes.length; i++){
						if(spiderTask.ifThisWorker(workNodes[i]) && !triggerNode.equals(workNodes[i])){
							workers[i] = workNodes[i];
							spiderTask.addWorker(workNodes[i]);
						}
					}
					//String[] workers = {"fafa" , "sdfa"};
					message.setWorkerNode(workers);
					String sendData = JsonUtils.objectToJson(message);
					staticSpiderPub.send(sendData , 0);
					LOGGER.info("Pub message to spider client:\n" + sendData);
				}
			}
		}
	}
	
	public static void main(String[] args){
		//SpiderTaskQueue.put("www.shouji.360.com", new SpiderTask("http://zhushou.360.cn/" , "PagePro360"));
		SpiderTaskQueue.put("m.163.com/android/", new SpiderTask("http://m.163.com/android/" , "PagePro163"));
		
		MdBroker broker = new MdBroker(true);
		//can be called multiple times with different endpoints
		broker.bind("tcp://*:5550" , "tcp://*:5551");
		broker.mediate();
	}
	
}
