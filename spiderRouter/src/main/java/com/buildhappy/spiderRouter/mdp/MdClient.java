package com.buildhappy.spiderRouter.mdp;

import java.util.Formatter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

/**
 * 异步的客户端
 * @author buildhappy
 *
 */
public class MdClient {
	private String broker;
	private ZContext ctx;
	private Socket client;
	private long timeout = 2500;
	private boolean verbose;
	private Formatter log = new Formatter(System.out);
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MdClient.class);
	
	public MdClient(String broker , boolean verbose){
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
		System.out.println("client waiting0");
		if(items.poll(timeout) == -1){
			return null;
		}
		System.out.println("client waiting1");
		if(items.pollin(0)){
			System.out.println("client waiting2");
			ZMsg msg = ZMsg.recvMsg(client);//非阻塞
			System.out.println("client waiting3");
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
		Executor executor = Executors.newCachedThreadPool();
		/**/
		boolean verbose = (args.length > 0 && "-v".equals(args[0]));
		verbose = true;
		final MdClient clientSession = new MdClient("tcp://localhost:5550", verbose);
		int count;
		executor.execute(new Runnable(){
			public void run() {
				int count = 0;
				while(true){
					ZMsg request = new ZMsg();
					request.addString("Hello world" + count++);
					clientSession.send("downloader", request);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		/*
		for (count = 0; count < 1; count++) {
			ZMsg request = new ZMsg();
			request.addString("Hello world" + count);
			clientSession.send("downloader", request);
		}*/
		Thread.sleep(10000);
		executor.execute(new Runnable(){
			public void run() {
				int count = 0;
				while(true){
					ZMsg reply = clientSession.recv();
					LOGGER.info("MdClient receive reply from broker:\n" + reply.toString());
					if (reply != null)
						reply.destroy();
					else
						break; // Interrupt or failure
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		/*
		for (count = 0; count < 1; count++) {
			
			ZMsg reply = clientSession.recv();
			if (reply != null)
				reply.destroy();
			else
				break; // Interrupt or failure
		}
		*/
		//System.out.printf("%d requests/replies processed\n", count);
		clientSession.destroy();
		
	}
}
