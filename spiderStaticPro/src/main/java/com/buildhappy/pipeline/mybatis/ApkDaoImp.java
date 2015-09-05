package com.buildhappy.pipeline.mybatis;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.buildhappy.pipeline.mybatis.domain.Apk;
import com.buildhappy.pipeline.mybatis.mapper.ApkMapper;
@Service("apkService")
@Repository
public class ApkDaoImp implements ApkDao{
	@Autowired
	private ApkMapper apkMapper;
	/*
	public Set<Apk> selectAllApk(){
		return apkMapper.selectAllApk();
	}
	public Apk selectApkById(String id){
		return apkMapper.selectApkById(id);
	}
	*/
	public void insertApk(Apk apk){
		apkMapper.insertApk(apk);
	}
	/*
	public void updateApk(Apk apk){
		apkMapper.updateApk(apk);
	}
	public void deleteById(String id){
		apkMapper.deleteById(id);
	}
	*/
	@Override
	public void createTable(String tableName) {
		apkMapper.createTable(tableName);
	}
	@Override
	public Set<Apk> selectAllApk(String tableName) {
		return apkMapper.selectAllApk(tableName);
	}
	@Override
	public int countColum(HashMap<String, String> data) {
		return apkMapper.countColum(data);
	}
}
