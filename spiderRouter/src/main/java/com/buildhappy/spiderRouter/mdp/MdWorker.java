package com.buildhappy.spiderRouter.mdp;

import java.util.Formatter;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

/**
 * 管家工人的api(P60)
 * @author buildhappy
 *
 */
public class MdWorker {
	private static final int HEARTBEAT_LIVENESS = 3;//3-5都可以
	private String broker;
	private ZContext ctx;
	private String service;
	private Socket worker;
	private long heartbeatAt;
	private long liveness;
	private int heartbeat = 25000;
	private int reconnect = 25000;
	
	private boolean expectReply = false;
	private long timeout = 2500;
	private boolean verbose;//是否需要将活动打印出来
	private Formatter log = new Formatter(System.out);
	
	private ZFrame replyTo;//返回地址
	
	public MdWorker(String broker , String service , boolean verbose){
		assert(broker != null);
		assert(service != null);
		this.broker = broker;
		this.service = service;
		this.verbose = verbose;
		ctx = new ZContext();
		reconnectToBroker();
	}
	//send message to broker.If no msg is provided,create one internally
	void sendToBroker(MDP command , String option , ZMsg msg){
		//msg.duplicate():Creates copy of this ZMsg.Also duplicates all frame content.
		msg = msg != null ? msg.duplicate():new ZMsg();
		if(option != null)
			msg.addFirst(new ZFrame(option));
		msg.addFirst(command.newFrame());
		msg.addFirst(MDP.DOWNLOAD_WORKER.newFrame());
		msg.addFirst(new byte[0]);//这就是dealer与req的不同之处，req在此处会自动加入一个空帧
		if(verbose){
			log.format("I:sending %s to broker\n", command);
			msg.dump(log.out());
		}
		msg.send(worker);
	}
	private void reconnectToBroker() {
		if(worker != null){
			ctx.destroySocket(worker);
		}
		worker = ctx.createSocket(ZMQ.DEALER);
		worker.connect(broker);
		if(verbose)
			log.format("I:connecting to broker at %s \n", broker);
		sendToBroker(MDP.W_READY , service , null);
		liveness = HEARTBEAT_LIVENESS;
		heartbeatAt = System.currentTimeMillis() + heartbeat;
	}
	
	//send reply,if any,to broker and wait for next reqeust
	public ZMsg receive(ZMsg reply){
		assert(reply != null || !expectReply);
		if(reply != null){
			assert(replyTo != null);
			reply.wrap(replyTo);
			sendToBroker(MDP.W_REPLY , null ,reply);
			reply.destroy();
		}
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
					log.format("I:received message from broker");
					msg.dump(log.out());
				}
				liveness = HEARTBEAT_LIVENESS;
				//don't try to handle errors,just assert noisily
				assert(msg != null && msg.size() >= 3);
				ZFrame empty = msg.pop();
				assert(empty.getData().length == 0);
				empty.destroy();
				
				ZFrame header = msg.pop();
				assert(MDP.DOWNLOAD_WORKER.frameEquals(header));
				header.destroy();
				
				ZFrame command = msg.pop();
				if(MDP.W_REQUEST.frameEquals(command)){
					//pop and save as many address as there are up to a null part,
					//for now ,just save one
					replyTo = msg.unwrap();
					command.destroy();
					return msg;//we have a request to process
				}else if(MDP.W_HEARTBEAT.frameEquals(command)){
					//do nothing for heartbeat
				}else if(MDP.W_DISCONNECT.frameEquals(command)){
					reconnectToBroker();
				}else{
					log.format("E:invalid input message\n");
					msg.dump(log.out());
				}
				command.destroy();
				msg.destroy();
			}else if(--liveness == 0){
				if(verbose){
					log.format("W:disconnect from broker - retrying");
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
			log.format("W:interrupt received,killing worker\n");
		}
		return null;
	}	
	
	public void destroy(){
		ctx.destroy();
	}
	
	public static void main(String[] args){
		boolean verbose = true;
		MdWorker workerSession = new MdWorker("tcp://127.0.0.1:5500" , "download" , verbose);
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
}
