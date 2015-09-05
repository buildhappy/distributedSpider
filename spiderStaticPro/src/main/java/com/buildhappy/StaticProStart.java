package com.buildhappy;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;

import com.buildhappy.pipeline.DBPipeline;
import com.buildhappy.pipeline.Pipeline;
import com.buildhappy.utils.ZMQUtils;

/**
 * 基本可靠队列/简单的海盗模式
 * 当连续3次没有收到应答时，会关闭原来的链接重新建立链接
 * 发送心跳服务，接受页面处理的任务，启动静态页面处理模块进行工作
 *  how to use:
 *    cd /home/buildhappy/Documents/jzmq-master/src/main/perl
 *    javac HLClient.java
 *    java -Djava.library.path=/usr/local/lib -classpath ./ HLClient
 * @author buildhappy
 *
 */
public class StaticProStart{// implements Runnable
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StaticProStart.class);
	private final static int REQUEST_RETRIES = 3;
	
	private static final Socket dealerClient = ZMQUtils.getDealerSocket();
	private static final Socket subClient = ZMQUtils.getSUBSocket();
	private final static PollItem items[] = {new PollItem(dealerClient , ZMQ.Poller.POLLIN),
			new PollItem(subClient , ZMQ.Poller.POLLIN)};
	static{
		//load the spring context
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:springBeans.xml");//new FileSystemXmlApplicationContext("springBeans.xml");
	}
	public static void main(String[] args) throws Exception{
		ScheduledThreadPoolExecutor exe = new ScheduledThreadPoolExecutor(1);
		//http://www.anzhi.com
		//http://apk.hiapk.com/ http://zhushou.360.cn/ http://m.163.com/android/
		String siteRoot = "http://zhushou.360.cn/";
		//Anzhi PageProHiApk PagePro360 PagePro163
		String pagePro = "PagePro360";
		Pipeline pipeline = new DBPipeline();
		StaticProSpider spider = StaticProSpider.create(pagePro , siteRoot).addPipeline(pipeline).addUrl(siteRoot);
		spider.start();
		//exe.schedule(command, delay, unit)
	}
}
