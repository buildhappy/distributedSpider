package com.buildhappy.spiderRouter;

import java.util.ArrayList;
import java.util.Iterator;

import org.zeromq.ZFrame;

/**
 * 封装后端工人
 * @author buildhappy
 *
 */
public class Worker {
	private final static int HEARTBEAT_INTERVAL = 1000;
	private final static int HEARTBEAT_LIVENESS = 3;
	ZFrame address;
	String identity;
	long expiry;
	protected Worker(ZFrame address){
		this.address = address;
		identity = new String(address.getData());
		expiry = System.currentTimeMillis() + HEARTBEAT_INTERVAL * HEARTBEAT_LIVENESS;
	}
	//在准备就绪列表的末尾放置一个工人
	protected void ready(ArrayList<Worker> workers){
		Iterator<Worker> it = workers.iterator();
		while(it.hasNext()){
			Worker worker = it.next();
			if(identity.equals(worker.identity)){
				it.remove();
				break;
			}
		}
		workers.add(this);
	}
	//获取下一个可用工人的方法
	protected static ZFrame next(ArrayList<Worker> workers){
		Worker worker = workers.remove(0);
		assert(worker != null);
		ZFrame frame = worker.address;
		return frame;
	}
	//寻找并清除过期的工人。保存从最旧到最新的工人，所以我们停在第一个“活着”的工人上
	protected static void purge(ArrayList<Worker> workers){
		Iterator<Worker> it = workers.iterator();
		while(it.hasNext()){
			Worker worker = it.next();
			if(System.currentTimeMillis() < worker.expiry){
				break;
			}
			it.remove();
		}
	}
}