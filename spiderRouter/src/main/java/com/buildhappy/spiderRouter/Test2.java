package com.buildhappy.spiderRouter;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;

public class Test2 {

	public static void main(String[] args) {
		SubTest sub = new SubTest();
		sub.start();
	}
	public static class SubTest{
		private ZContext ctx;
		private Socket sub;
		public SubTest(){
			this.ctx = new ZContext();
			this.sub = ctx.createSocket(ZMQ.SUB);
			sub.connect("tcp://127.0.0.1:5550");
			this.sub.subscribe("".getBytes());
		}
		public void start(){
			while(true){
				PollItem[] items = {new PollItem(sub , ZMQ.Poller.POLLIN)};
				int rc = ZMQ.poll(items, 1000);
				System.out.println("rc:" + rc);
				if(rc == -1){
					break;
				}
				if(items[0].isReadable()){
					ZMsg msg = ZMsg.recvMsg(sub, 1);
					System.out.println(msg);
				}
			}
		}
	}
}
