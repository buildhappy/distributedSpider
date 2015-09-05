package com.buildhappy.dynamicModel.client;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;

/**
 * consume the download task from the cached container
 * @author buildhappy
 *
 */
public class PerformDynamicTask implements Runnable{
	Socket worker;
	private static Executor executor = Executors.newFixedThreadPool(10);
	private volatile BlockingQueue<ZMsg> downloadTask;
	public PerformDynamicTask(Socket worker , BlockingQueue<ZMsg> downloadTask){
		this.worker = worker;
		this.downloadTask = downloadTask;
	}
	
	public synchronized void performDonwloadTask(){
		Iterator<ZMsg> it = downloadTask.iterator();
		if(it.hasNext()){
			ZMsg msg = it.next();
			executor.execute(new DynamicUtil(worker , msg));
			it.remove();
		}
	}
	public void run() {
		while(!Thread.currentThread().isInterrupted()){
			performDonwloadTask();
			//System.out.println("downloadTask size:" + downloadTask.size());
		}
	}

}
