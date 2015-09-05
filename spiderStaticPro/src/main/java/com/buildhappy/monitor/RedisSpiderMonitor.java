package com.buildhappy.monitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;

import com.buildhappy.selector.thread.CountableThreadPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


//开启memcached:memcached -m 10 -d -u buildhappy -l 127.0.0.1 -p 11211 -c 32 -P /tmp/memcached.pid
//关闭memcached:ps -ef | grep memcached 
public class RedisSpiderMonitor implements Runnable{
	static Jedis jedis = new Jedis ("127.0.0.1",6379);//连接redis
	protected CountableThreadPool threadPool = new CountableThreadPool(4);
	//http://m.163.com/android/ http://zhushou.360.cn/
	//http://www.anzhi.com http://apk.hiapk.com/
	private static String siteRoot = "http://zhushou.360.cn/";
	
	private static String root = "360_eight";
	
	private static boolean flag = true;
	
	private static String todoFileName = root + "_todoUrl.txt";
	private static String doneFileName = root + "_doneUrl.txt";
	private static File toDoUrlFile = new File("/home/buildhappy/log/" + todoFileName);
	private static File doneUrlFile = new File("/home/buildhappy/log/" + doneFileName);
	private static FileWriter todoWriter;
	private static FileWriter doneWriter;
	static{
		try {
			todoWriter = new FileWriter(toDoUrlFile);
			doneWriter = new FileWriter(doneUrlFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException {
		//jedis.flushDB();
		/*
		RedisClientTest test = new RedisClientTest();
		Thread thread = new Thread(test);
		thread.start();
		*/
		spider_keys();
		//统计个数
		spider_values();
		
	}
	public static void initRedis(){
		JedisPoolConfig config = new JedisPoolConfig(); 
        //config.setMaxActive(20); 
        config.setMaxIdle(5); 
        config.setMaxWaitMillis(1000l); 
        config.setTestOnBorrow(false); 
        JedisPool jedisPool = new JedisPool(config,"127.0.0.1",6379);
        jedisPool.getResource();
        //jedisPool.
	}
	public static void set_get(){
		for(int i = 0; i < 10000; i++){
			jedis.set("key" + i, "hello");
		}
		Set<String> keys = jedis.keys("key*");
		for(String key : keys){
			System.out.println(key);
		}
	}
	
	//hash操作
	public static void mset_hmget(){
		//将hash表key中的域field的值设为value
		//jedis.flushDB();
		//String result = redis.hget("http://zhushou.360.cn/", "field1");
		//redis.hset("http://zhushou.360.cn/", "field1", "hello1");
		//redis.hset("http://zhushou.360.cn/", "field2", "hello1");
		long startTime = System.nanoTime();
		for(int i = 0; i < 10000; i++){
			jedis.hset(siteRoot, "http://zhushou.360.cn/fafsafsdfdfasgfggafdsgfg/geeg43gs" + i, "hello1");
		}
		
		Set<String> keys = jedis.hkeys(siteRoot);
		System.out.println("keys size:" + keys.size());
		for(String key : keys){
			jedis.hset("siteRoot" , key, "1");
			//System.out.println(key);
		}
		System.out.println(((System.nanoTime() - startTime) / 1000) / 1000 + "秒");
		//System.out.println("hset:" + redis.hset("key11", "field1", "hello1"));
	}
	//所有的url个数,以处理+未处理
	public static void spider_keys(){
		Set<String> keys = spider_keys_set();
		/*
		for(String key : keys){
			System.out.println(key);
		}
		*/
		System.out.println("total size:" + keys.size());
	}
	//统计处理(done)url和未处理的(todo)url
	public static void spider_values() throws IOException{
		
		int todoSize = 0, doneSize = 0;
		Set<String> keys = spider_keys_set();
		for(String key : keys){
			String status = jedis.hget(siteRoot, key);
			if(status != null && status.equals("1")){
				if(flag){
					todoWriter.write(key);
					todoWriter.write("\n");
					todoWriter.flush();
				}

				todoSize++;
			}else if(status != null && status.equals("0")){
				if(flag){
					doneWriter.write(key);
					doneWriter.write("\n");
					doneWriter.flush();
				}

				doneSize++;
			}
		}
		System.out.println("todo tasks:" + todoSize);
		System.out.println("done tasks:" + doneSize);
	}
	
	public static Set<String> spider_keys_set(){
		Set<String> keys = jedis.hkeys(siteRoot);
		//jedis.hdel(siteRoot , siteRoot);
		return keys;
	}
	
	public boolean test_hget(String key){
		System.out.println(Thread.currentThread().getName() + " test_hget:" + key);
		return (jedis.hget(siteRoot, key) == null);
	}
	
	public void test_hset(){
		while(true){
			Random seed = new Random();
			int rand = seed.nextInt(100000);
			String key = "key" + rand;
			System.out.println(Thread.currentThread().getName() + " test_hset:" + key);
			if(test_hget(key)){
				jedis.hset("http://zhushou.360.cn/", key, "33");
			}
		}
	}
	
	public void run() {
		threadPool.execute(new Runnable(){
			public void run(){
				test_hset();
			}
		}
		);
	}
}
