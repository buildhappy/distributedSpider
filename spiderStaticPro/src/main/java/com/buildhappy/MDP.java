package com.buildhappy;

import java.util.Arrays;

import org.zeromq.ZFrame;

/**
 * ZeroMQ Majordomo Protocol 验证协议
 * 用于实现多client、多worker实现双向指定目标数据接收
 * 此为常量类
 * @author buildhappy
 *
 */
public enum MDP {
	C_CLIENT("MDPC01") , DOWNLOAD_WORKER("MDPW01"),DYNAMIC_WORKER("MDPW02"),//W_WORKER("MDPW01"),
	W_READY(1) , W_REQUEST(2) , W_REPLY(3) , W_HEARTBEAT(4) , W_DISCONNECT(5) , 
	DOWNLOAD_DONE(6),DYNAMIC_DONE(7);
	private final byte[] data;
	
	MDP(String value){
		this.data = value.getBytes();
	}
	
	MDP(int value){
		byte b = (byte)(value & 0xFF);
		this.data = new byte[]{b};
	}
	
	public ZFrame newFrame(){
		return new ZFrame(data);
	}
	
	public boolean frameEquals(ZFrame frame){
		return Arrays.equals(data, frame.getData());
	}
}
