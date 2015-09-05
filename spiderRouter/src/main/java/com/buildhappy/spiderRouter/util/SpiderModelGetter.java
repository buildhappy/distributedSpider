package com.buildhappy.spiderRouter.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.buildhappy.spiderRouter.model.StaticProModel;

public class SpiderModelGetter {
	private static final double loadFactor = 1;
	private final Map<String , StaticProModel> staticModel;
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpiderModelGetter.class);
	public SpiderModelGetter(Map<String , StaticProModel> staticModel){
		this.staticModel = staticModel;
	}
	public void monitor(){
		Set<String> keys = staticModel.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			System.out.println(staticModel.get(it.next()).getIdentity());
		}
	}
	/**
	 * get the spider model that has the least task
	 * @return
	 */
	public synchronized String getTriggerNode(){
		
		//System.out.println(mapSize());
		StaticProModel model = getLeastTaskStaticModel();
		LOGGER.info("SpiderModelGetter getTriggerNode() " + model);
		if(model != null){
			return model.getIdentity();
		}
		
		return null;
	}
	public synchronized String[] getWorkerNodes(){
		int nodesNum = (int)(mapSize() * loadFactor);
		String[] nodes = new String[nodesNum];
		String ss = null;
		for(int i = 0; i < nodesNum; i++){
			nodes[i] = getLeastTaskStaticModel().getIdentity();
			ss += (" " + nodes[i]);
		}
		ss = ss.replace("null ", "");
		LOGGER.info("SpiderModelGetter getWorkerNodes() " + ss);
		return nodes;
	}
	public synchronized StaticProModel getLeastTaskStaticModel(){
		Set<String> keys = staticModel.keySet();
		if(keys == null || keys.size() <= 0)
			return null;
		Iterator<String> it = keys.iterator();
		String key = null;
		StaticProModel model = null;
		//System.out.println("print map:");
		if(it.hasNext()){
			key = it.next();
			//System.out.print(key + " ");
			model = staticModel.get(key);
		}
		//System.out.println();
		
		while(it.hasNext()){
			String keyC = it.next();
			StaticProModel modelC = staticModel.get(keyC);
			if(modelC.getTaskCounter() < model.getTaskCounter()){
				key = keyC;
				model = modelC;
			}
		}
		staticModel.remove(key);
		model.incrementTaskCounter();
		staticModel.put(key, model);
		return model;
	}
	public int mapSize(){
		Set<String> keys = staticModel.keySet();
		if(keys == null || keys.size() <= 0)
			return 0;
		return keys.size();
	}
	public static void main(String[] args) {
		StaticProModel t1 = new StaticProModel("id1");
		t1.setTaskCounter(1);
		t1.setIdentity("ip1");
		
		StaticProModel t2 = new StaticProModel("id2");
		t2.setTaskCounter(3);
		t2.setIdentity("ip2");
		
		
		StaticProModel t3 = new StaticProModel("id3");
		t3.setTaskCounter(4);
		t3.setIdentity("ip3");
		
		HashMap<String , StaticProModel> map = new HashMap<String , StaticProModel>();
		map.put("id1" , t1);
		map.put("id2" , t2);
		map.put("id3" , t3);

		SpiderModelGetter spiderGetter = new SpiderModelGetter(map);
		spiderGetter.monitor();
		System.out.println("getTriggerNode:" + spiderGetter.getTriggerNode());
		String[] ss = spiderGetter.getWorkerNodes();
		//System.out.println(ss.toString());
		for(int i = 0; i < ss.length; i++){
			System.out.println(ss[i]);
		}
	}
}
