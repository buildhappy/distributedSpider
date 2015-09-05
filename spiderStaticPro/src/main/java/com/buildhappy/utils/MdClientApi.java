package com.buildhappy.utils;

import java.util.Formatter;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.buildhappy.MDP;

/**
 * Majordomo Protocol Client API, asynchronous
 * @author buildhappy
 *
 */
public class MdClientApi {
	private String broker;
	private ZContext ctx;
	private Socket client;
	private long timeout = 2500;
	private boolean verbose;
	private Formatter log = new Formatter(System.out);
	
	public MdClientApi(String broker){
		this(broker , true);
	}
	public MdClientApi(String broker , boolean verbose){
		this.broker = broker;
		this.verbose = verbose;
		ctx = new ZContext();
		reconnectToBroker();
	}
	
	/**
	 * 链接或重新链接服务器
	 */
	private void reconnectToBroker() {
		if(client != null){
			//ctx.destroySocket(client);
		}
		client = ctx.createSocket(ZMQ.DEALER);
		//client.setIdentity("client01".getBytes());
		client.connect(broker);
		if(verbose){
			log.format("I:connecting to broker at %s\n", broker);
		}
	}
	
	public ZMsg recv(){
		ZMsg reply = null;
		Poller items = new Poller(1);
		items.register(client , Poller.POLLIN);
		if(items.poll(timeout * 1000) == -1){
			return null;
		}
		
		if(items.pollin(0)){
			ZMsg msg = ZMsg.recvMsg(client , 0);//非阻塞
			if(verbose){
				log.format("I:receive reply");
				msg.dump(log.out());
			}
			assert(msg.size() >= 4);
			ZFrame empty = msg.pop();
			assert(empty.getData().length == 0);
			empty.destroy();
			ZFrame header = msg.pop();
			assert(MDP.C_CLIENT.equals(header.toString()));
			header.destroy();
			
			ZFrame replyService = msg.pop();
			replyService.destroy();
			
			reply = msg;
		}
		return reply;
	}
	/**
	 * 发送的消息格式：
	 * [000] 
	 * [006] MDPC01
	 * [004] echo
	 * [011] Hello world
	 * @param service
	 * @param request
	 */
	public void send(String service , ZMsg request){
		assert(request == null);
		request.addFirst(service);//在reqeust前面添加帧
		request.addFirst(MDP.C_CLIENT.newFrame());
		request.addFirst("");
		if(verbose){
			log.format("I:send request to '%s' service.\n", service);
			request.dump(log.out());
		}
		request.send(client);
	}
	
	public void destroy(){
		ctx.destroy();
	}
	
	public static void main(String[] args) throws InterruptedException{
		boolean verbose = (args.length > 0 && "-v".equals(args[0]));
		verbose = true;
		MdClientApi clientSession = new MdClientApi("tcp://localhost:5500", verbose);
		int count;
		for (count = 0; count < 100; count++) {
			ZMsg request = new ZMsg();
			request.addString("Hello world");
			clientSession.send("echo", request);
		}
		for (count = 0; count < 100; count++) {
			ZMsg reply = clientSession.recv();
			if (reply != null)
				reply.destroy();
			else
				break; // Interrupt or failure
		}
		System.out.printf("%d requests/replies processed\n", count);
		clientSession.destroy();
		
	}
}
