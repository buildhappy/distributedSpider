package com.buildhappy.pipeline;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buildhappy.ResultItems;
import com.buildhappy.Task;
import com.buildhappy.utils.FilePersistentBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Store results in files.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@ThreadSafe
public class FilePipeline extends FilePersistentBase implements Pipeline {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static String taskId;
	private static String channelId;
    private File file;
    private PrintWriter printWriter;//=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)),true);
    public FilePipeline(String taskId , String channelId){
    	this.taskId = taskId;
    	this.channelId = channelId;
    	initSource();
    	printWriter.print("channelId:" + channelId + "\n");
    }
    private void initSource(){
    	file = new File(taskId + "_" + channelId + ".txt");
    	try {
			printWriter=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)),true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

    public void process(ResultItems result, Task task) {
    	logger.info("in FilePipeline");
        /*
		Apk apk = result.get("apk");
		String apkStr = JsonUtils.objectToJson(apk);
		
		try {
			printWriter.print(apkStr + "\n");
			printWriter.flush();
		}catch (Exception e) {
            logger.warn("write file error", e);
        }
        */
    }
    
}
