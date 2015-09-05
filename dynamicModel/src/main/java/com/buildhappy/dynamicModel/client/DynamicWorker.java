package com.buildhappy.dynamicModel.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;

import com.buildhappy.util.PropertiesUtil;

/**
 * how to use:
 *   cd /home/buildhappy/Documents/jzmq-master/src/main/perl
 *   javac HLClient.java
 *   java -Djava.library.path=/usr/local/lib -classpath ./ HLClient
 * @author buildhappy
 *
 */
public class DynamicWorker {
//	private ZContext ctx;
//	private Socket worker;
	public static void main(String[] args) {

		ZContext ctx = new ZContext();
		Socket worker;
		String brokerAddress = PropertiesUtil.getRouterDealerAddress();
		String service = "dynamic";
		worker = ctx.createSocket(ZMQ.DEALER);
		worker.connect(brokerAddress);
		System.out.println("connect to broker:" + brokerAddress);
		
		//List<ZMsg> downloadTask = Collections.synchronizedList(new ArrayList<ZMsg>());
		BlockingQueue<ZMsg> downloadTaskQueue = new LinkedBlockingQueue<ZMsg>();
		PerformDynamicTask performDownloadTask = new PerformDynamicTask(worker , downloadTaskQueue);
		new Thread(performDownloadTask).start();

		boolean verbose = true;
		MdDynamicApi workerSession = new MdDynamicApi(worker ,service, verbose , downloadTaskQueue);
		ZMsg reply = null;
		while(!Thread.currentThread().isInterrupted()){
			ZMsg request = workerSession.receive(reply);
			if(request == null){
				break;
			}
			reply = request;//echo is complex
		}
		//workerSession.destroy();

	}

}
