package com.buildhappy.dynamicModel.client;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;


/**
 * the api to zeromq broker
 * @author buildhappy
 *
 */
public class MdDynamicApi {
	private static final int HEARTBEAT_LIVENESS = 3;//3-5都可以
	private String service;
	private Socket worker;
	private long heartbeatAt;
	private long liveness;
	private int heartbeat = 25000;
	private int reconnect = 25000;
	
	private boolean expectReply = false;
	private long timeout = 25000;
	private boolean verbose;//是否需要将活动打印出来
	//private Formatter log = new Formatter(System.out);
	//the container to store the download task,it's the cache of consumer and producer 
	private final BlockingQueue<ZMsg> dynamicTask ;//= Collections.synchronizedList(new ArrayList<ZMsg>());
	private org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MdDynamicApi.class);
	
	//private ZFrame replyTo;//返回地址
	
	public MdDynamicApi(Socket worker , String service , boolean verbose , BlockingQueue<ZMsg> downloadTask){
		////LOGGER.info("MdDynamicApi constructor");
		assert(service != null);
		this.service = service;
		this.verbose = verbose;
		this.worker = worker;
		this.dynamicTask = downloadTask;
		reconnectToBroker();
	}
	//send message to broker.If no msg is provided,create one internally
	void sendToBroker(MDP command , String option , ZMsg msg){
		//LOGGER.info("MdDynamicApi sendToBroker()");
		//msg.duplicate():Creates copy of this ZMsg.Also duplicates all frame content.
		msg = msg != null ? msg.duplicate():new ZMsg();
		if(option != null)
			msg.addFirst(new ZFrame(option));
		msg.addFirst(command.newFrame());
		msg.addFirst(MDP.DYNAMIC_WORKER.newFrame());
		msg.addFirst(new byte[0]);//这就是dealer与req的不同之处，req在此处会自动加入一个空帧
		if(verbose){
			//LOGGER.info("I:sending %s to broker\n", command);
			
			//LOGGER.info(msg.toString());
		}
		msg.send(worker);
	}
	private void reconnectToBroker() {
		//if(worker != null){
			//ctx.destroySocket(worker);
		//}
		//worker = ctx.createSocket(ZMQ.DEALER);
		//worker.connect(broker);
		//if(verbose)
			//log.format("I:connecting to broker at %s \n", broker);
		sendToBroker(MDP.W_READY , service , null);
		liveness = HEARTBEAT_LIVENESS;
		heartbeatAt = System.currentTimeMillis() + heartbeat;
	}
	
	//send reply,if any,to broker and wait for next reqeust
	public ZMsg receive(ZMsg reply){
		assert(reply != null || !expectReply);
		expectReply = true;
		while(!Thread.currentThread().isInterrupted()){
			Poller items = new Poller(1);
			items.register(worker , ZMQ.Poller.POLLIN);
			if(items.poll(timeout) == -1){
				break;
			}
			if(items.pollin(0)){
				ZMsg msg = ZMsg.recvMsg(worker);
				if(msg == null){
					break;
				}
				if(verbose){
					//LOGGER.info("I:received message from broker");
					//received message from broker:
					//[000] 		empty frame
					//[006] MDPW01  mdp worker identity
					//[001] 04		mdp worker status,eg.READY(1),REQUEST(2),REPLY(3),HEARTBEAT(4),DISCONNECT(5);
					//[000] ffsafs	message body		
					//msg.dump(log.out());
					
					//LOGGER.info(msg.toString());
					System.out.println("I:received message from broker");
					System.out.println(msg.toString());
				}
				liveness = HEARTBEAT_LIVENESS;
				//don't try to handle errors,just assert noisily
				assert(msg != null && msg.size() >= 3);
				ZFrame empty = msg.pop();//empty frame
				assert(empty.getData().length == 0);
				empty.destroy();
				
				ZFrame header = msg.pop();
				assert(MDP.DOWNLOAD_WORKER.frameEquals(header));
				header.destroy();
				
				ZFrame command = msg.pop();
				if(MDP.W_REQUEST.frameEquals(command)){
					//此时msg的消息格式:
					//[005] 006B8B456A
					//[000] 
					//[011] Hello world
					dynamicTask.add(msg);
					//msg.dump(log.out());
					//replyTo = msg.unwrap();
					
					command.destroy();
					return msg;//we have a request to process
				}else if(MDP.W_HEARTBEAT.frameEquals(command)){
					//do nothing for heartbeat
				}else if(MDP.W_DISCONNECT.frameEquals(command)){
					reconnectToBroker();
				}else{
					//LOGGER.info("E:invalid input message\n");
					//LOGGER.info(msg.toString());
					//msg.dump(log.out());
				}
				command.destroy();
				msg.destroy();
			}else if(--liveness == 0){
				if(verbose){
					//LOGGER.info("W:disconnect from broker - retrying");
				}
				try{
					Thread.sleep(reconnect);
				}catch(InterruptedException e){
					Thread.currentThread().interrupt();
					break;
				}
				reconnectToBroker();
			}
			//send HEARTBEAT if it's time
			if(System.currentTimeMillis() > heartbeatAt){
				sendToBroker(MDP.W_HEARTBEAT , null , null);
				heartbeatAt = System.currentTimeMillis() + heartbeat;
			}
		}
		if(Thread.currentThread().isInterrupted()){
			//LOGGER.info("W:interrupt received,killing worker\n");
			System.out.println("W:interrupt received,killing worker\n");
		}
		return null;
	}	
	/*
	public void destroy(){
		ctx.destroy();
	}*/

	/*
	public static void main(String[] args){
		List<ZMsg> downloadTask = Collections.synchronizedList(new ArrayList<ZMsg>());
		PerformDownloadTask performDownloadTask = new PerformDownloadTask("",downloadTask);
		new Thread(performDownloadTask).start();
		System.out.println("fasfsfgege");
		boolean verbose = true;
		MdDynamicApi workerSession = new MdDynamicApi("tcp://127.0.0.1:5550" , "downloader" , verbose , downloadTask);
		ZMsg reply = null;
		while(!Thread.currentThread().isInterrupted()){
			ZMsg request = workerSession.receive(reply);
			if(request == null){
				break;
			}
			reply = request;//echo is complex
		}
		workerSession.destroy();
	}
	*/
}
