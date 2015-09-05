package com.buildhappy.spiderRouter;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import com.buildhappy.spiderRouter.model.StaticProModel;

public class TestTreeMap {

	public static void main(String[] args) {
		StaticProModel t1 = new StaticProModel("id1");
		t1.setTaskCounter(1);
		
		StaticProModel t2 = new StaticProModel("id2");
		t2.setTaskCounter(2);
		
		StaticProModel t3 = new StaticProModel("id3");
		t3.setTaskCounter(3);
		/**/
		TreeMap<String , StaticProModel> treeMap = new TreeMap<String , StaticProModel>();

		
		treeMap.put("id1", t1);
		treeMap.put("id2", t2);
		treeMap.put("id3", t3);
		//StaticProModel[] values = (StaticProModel[]) treeMap.values().toArray();
		Object[] values = treeMap.values().toArray();
		for(Object value : values)
			System.out.println(((StaticProModel)value).getRouterAddress());
		/*
		//Comparator comparator = new StaticProModelCom();
		TreeSet<StaticProModel> treeSet = new TreeSet<StaticProModel>(new StaticProModelComparator());
		treeSet.add(t3);
		treeSet.add(t1);
		treeSet.add(t2);
		
		t2.setTaskCounter(6);
		System.out.println(treeSet.remove(t3));
		treeSet.add(t3);
		
		System.out.println(treeSet.last().getAddress());
		System.out.println(treeSet.first().getAddress());
		System.out.println(treeSet.size());
		System.out.println();
		for(StaticProModel p : treeSet)
			System.out.println(p.getAddress());
		*/
	}

}

class StaticProModelComparator implements Comparator<StaticProModel>{

	public int compare(StaticProModel o1, StaticProModel o2) {
		
		StaticProModel obj1 = (StaticProModel) o1;
		StaticProModel obj2 = (StaticProModel) o2;
		int flag = 0;
		if(obj1.getRouterAddress().equals(obj2.getRouterAddress())){
			return 0;
		}
		if(obj1.getTaskCounter() > obj2.getTaskCounter())
			flag = 1;
		else if(obj1.getTaskCounter() < obj2.getTaskCounter())
			flag = -1;
		System.out.println(obj1.getRouterAddress() + " in comparator " + obj2.getRouterAddress() + " " + flag);
		return flag;
	}
	
}
