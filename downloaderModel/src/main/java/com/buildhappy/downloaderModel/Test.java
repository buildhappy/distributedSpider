package com.buildhappy.downloaderModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.zeromq.ZMsg;

public class Test {

	public static void main(String[] args) {
		List<ZMsg> downloadTask = Collections.synchronizedList(new ArrayList<ZMsg>());
		ZMsg m1 = new ZMsg();
		m1.add("m1m1m1m1");
		ZMsg m2 = new ZMsg();
		m2.add("m2m2m2m2");
		downloadTask.add(m1);
		downloadTask.add(m2);
		Iterator<ZMsg> it = downloadTask.iterator();
		while(it.hasNext()){
			ZMsg m = it.next();
			System.out.println(m.toString());
			it.remove();
		}
		System.out.println(downloadTask.size());
	}

}
