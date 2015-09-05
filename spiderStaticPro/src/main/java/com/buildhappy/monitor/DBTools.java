package com.buildhappy.monitor;

import java.util.HashMap;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.buildhappy.pipeline.mybatis.ApkDao;
import com.buildhappy.pipeline.mybatis.domain.Apk;
/**
 * 查看爬虫结果
 * @author buildhappy
 *
 */
public class DBTools {
	private static final ApplicationContext context  = new ClassPathXmlApplicationContext("classpath:springBeans.xml");
	private static final ApkDao apkDao = context.getBean("apkService", ApkDao.class);
	
	public static void main(String[] args) {
		HashMap<String , String> data = new HashMap<String , String>();
		String tableName = "360_one";
		data.put("columName", "appDescription");
		data.put("tableName", tableName);
		System.out.println(apkDao.countColum(data));
		Set<Apk> datas = apkDao.selectAllApk(tableName);
		for(Apk apk : datas){
			//System.out.println(apk.getAppName());
		}
	}

}
