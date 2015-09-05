package com.buildhappy.downloaderModel;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.buildhappy.downloaderModel.util.DownloaderUtil;

public class FutureTaskWorker {
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	public static String startWork(final String downloadUrl) {
		//ExecutorService exe = Executors.newCachedThreadPool();
		/*
		FutureTask<String> future = new FutureTask<String>(new Callable<String>(){

			public String call() throws Exception {
				DownloaderUtil downloader = new DownloaderUtil();
				System.out.println("work thread:" + Thread.currentThread().getName());
				return downloader.download(downloadUrl, null);
			}
			
		});
		*/
		FutureTask<String> futureTask = new FutureTask<String>(new DownloaderUtil(downloadUrl , null));
		executor.execute(futureTask);
		
		try {
			//System.out.println(future.get(20000, TimeUnit.MILLISECONDS));
			return futureTask.get();
			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
		//System.out.println("main thread:" + Thread.currentThread().getName());
	}

}
