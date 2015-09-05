package com.buildhappy.pipeline.mybatis;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.buildhappy.pipeline.mybatis.domain.Apk;

public interface ApkDao {
	//public Set<Apk> selectAllApk();
	//public Apk selectApkById(String id);
	public void createTable(String tableName);
	public void insertApk(Apk apk);
	public Set<Apk> selectAllApk(String tableName);
	public int countColum(HashMap<String , String> data);
	//public void updateApk(Apk apk);
	//public void deleteById(String id);
	
}
