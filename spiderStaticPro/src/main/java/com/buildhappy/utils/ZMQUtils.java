package com.buildhappy.utils;

import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
@Component
public class ZMQUtils {
	private static final String ROUTER_DEALER = PropertiesUtil.getRouterDealerAddress();
	private static final String ROUTER_PUB = PropertiesUtil.getRouterPubAddress();
	private static ZContext CTX = new ZContext();
	private static final Socket dealerSocket;
	private static final Socket subSocket;
	private static final Socket reqSocket;
	
	static{
		dealerSocket = CTX.createSocket(ZMQ.DEALER);
		dealerSocket.connect(ROUTER_DEALER);//tcp://127.0.0.1:5555
		
		subSocket = CTX.createSocket(ZMQ.SUB);
		subSocket.connect(ROUTER_PUB);
		//System.out.println(ROUTER_PUB);
		subSocket.subscribe("".getBytes());
		subSocket.setIdentity("subSocket".getBytes());
		
		reqSocket = CTX.createSocket(ZMQ.REQ);
		reqSocket.connect(ROUTER_DEALER);
	}
	public static Socket getDealerSocket(){
		//Socket dealerSocket = CTX.createSocket(ZMQ.DEALER);
		//dealerSocket.connect(SERVER_ENDPOINT);//tcp://127.0.0.1:5555
		return dealerSocket;
	}
	
	public static Socket getSUBSocket(){
		//Socket subSocket = CTX.createSocket(ZMQ.SUB);
		//subSocket.connect("tcp://127.0.0.1:5550");
		//subSocket.subscribe("".getBytes());
		return subSocket;
	}
	
	public static Socket getReqSocket(){
		return reqSocket;
	}
	
	public static void main(String[] args){
		Socket s1 = ZMQUtils.getDealerSocket();
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Socket s2 = ZMQUtils.getDealerSocket();
		System.out.println(s1 == s2);
	}
}
