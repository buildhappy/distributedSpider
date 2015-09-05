package com.buildhappy.downloaderModel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;

import com.buildhappy.downloaderModel.bean.MDP;
/**
 *
 * @author greatdreams
 */
public class DownloaderUtil implements Callable<String>,Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderUtil.class.getName());
    private Formatter log = new Formatter(System.out);
    private ZFrame sendTo;
    private String downloadUrl;
    private String cookie;
    Socket worker;
    
    /**
     * The msg type of msg send from client:
     * [005] 006B8B456A	  //the address of client
     * [000] 
     * [011] http://ww..  //the download url
     * [000] cookie		  //the cookie for download resource 
	 */
    private ZMsg msg;
    public DownloaderUtil(Socket worker , ZMsg msg){
    	LOGGER.info("Download msg from broker: \n" + msg.toString());
    	this.worker = worker;
    	assert(msg.size() >= 3);
    	this.msg = msg;
    	sendTo = msg.unwrap();
    	//msg.pop();
    	ZFrame m = msg.pop();
    	downloadUrl = m.toString();
    	LOGGER.info("DownloadUrl from broker(m.toString()):" + downloadUrl);
    	//downloadUrl = m.strhex();
    	//LOGGER.info("DownloadUrl from broker(m.strhex()):" + downloadUrl);
    	if(!msg.isEmpty())
    		cookie = msg.pop().strhex();
    	
    }
    public DownloaderUtil(String downloadUrl){
    	this.downloadUrl = downloadUrl;
    }
    public DownloaderUtil(String downloadUrl , String cookie){
    	this(downloadUrl);
    	this.cookie = cookie;
    }
    public String download(){//, ConfigProperties configProperties) {
        LOGGER.info("download(appurl = " + downloadUrl + ", cookie = " + cookie + ")");

        int status = 1; // app download status flag
        String statusDescription = "-- app resource is unavailable --"; // description for downloading status
        String storagePath = "";//configProperties.getPath(); // the directory for storage of downloaded android apps
        
        String startTime = "---";
        String endTime = "---";
        long duration = 0;

        // UUID uuid = UUID.randomUUID();
        // String fileName = uuid.toString(); //downloaded app name which is a java UUID value.
        // String fileName = String.valueOf(url.hashCode()); // use the hashcode of url as the filename
        String fileName = StringToSHA1.toSHA1(downloadUrl); // use the hashcode of url as the filename
        long fileSize = 0;

        String headers[] = {
            "User-Agent:Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0 Iceweasel/31.4.0",
            "Connection:keep-alive",
            "Cache-Control:max-age=0",
            "Accept-Language:en-US,en;q=0.5",
            "Accept-Encoding:gzip, deflate",
            "Accept:ext/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Set-Cookie:"  + cookie
        };

        List<String> cmd = new ArrayList<>();
        /*
        cmd.add("curl");
        for (String header : headers) {
            cmd.add("-H");
            cmd.add(header);
        }
        cmd.add("--cookie");
        cmd.add(cookie);
        cmd.add("-C"); // the download will continue the last task if the last download task is interrupted 
        cmd.add("-");
        cmd.add("--location");
        cmd.add("-o");
        cmd.add(fileName);
        cmd.add(url);
        */
        
        /*
        cmd.add("wget");
        for(String header: headers) {
            cmd.add("--header");
            cmd.add(header);
        }
        cmd.add("-c");
        cmd.add("-O");
        cmd.add(fileName);
        cmd.add(downloadUrl);
        

        Process process;
        BufferedReader br;
        int returnValue = 1;

        try {
            //test whether the appurl is available
            process = new ProcessBuilder("curl", "-I", "--location", "-H", "User-Agent:Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0 Iceweasel/31.4.0", downloadUrl).start();
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            returnValue = process.waitFor();

            if (returnValue == 0) {
                while (br.ready()) {
                    String statusLine = br.readLine();
                    int statusCode = Integer.parseInt(statusLine.substring(9, 12));
                    if (status != 404) {
                       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                       Calendar calendar = Calendar.getInstance();
                       long startTimeMillis = System.currentTimeMillis();
                       calendar.setTimeInMillis(startTimeMillis);
                       startTime = sdf.format(calendar.getTime());
                       process = new ProcessBuilder(cmd).directory(new File(storagePath)).start();
                       returnValue = process.waitFor();
                       if (returnValue == 0) {
                           status = 0;
                           statusDescription = "--app downloading sucess---";
                       }
                       long endTimeMillis = System.currentTimeMillis();
                       calendar.setTimeInMillis(endTimeMillis);
                       endTime = sdf.format(calendar.getTime());
                       duration = endTimeMillis - startTimeMillis;
                    }
                    logger.debug("HTTP request final status code is " + statusCode);
                    break;
                }
            }
            process.destroy();
        } catch (IOException | InterruptedException ex) {
            logger.warn(ex.getMessage());
        }
        if (status == 1) {
            fileName = null;
            storagePath = null;
            startTime = "---";
            endTime = "---";
            duration = -1;
            fileSize = -1;
        }
        */
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        fileSize = 12;//new File(configProperties.getPath() + "/" + fileName).length();
        String result = "";
        result = downloadUrl + "-" +  status + "-" + storagePath + "/" + fileName;
        /*
                = "{"
                + "\"downloadUrl\" : \"" + downloadUrl + "\", "
                + "\"status\" : \"" + (status == 0 ? 4 : 0) + "\", "
                + "\"tmpLocation\" : \"" + storagePath + "/" + fileName + "\","
                + "\"fileSize\" : " + fileSize + ","
                + "\"startTime\" : \"" + startTime + "\","
                + "\"endTime\" : \"" + endTime + "\","
                + "\"duration\" : " + duration + ""
                + "}";*/
        LOGGER.debug("download return " + result);
        return result;
    }

	@Override
	public String call() throws Exception {
		return download();
	}
	@Override
	public void run() {
		LOGGER.info("DownloaderUtil run()");
		String result = download();
		ZMsg msg = new ZMsg();
		msg.addFirst(result);
		msg.addFirst(new byte[0]);
		msg.wrap(sendTo);
		msg.addFirst(MDP.W_REPLY.newFrame());
		msg.addFirst(MDP.DOWNLOAD_WORKER.newFrame());
		msg.addFirst(new byte[0]);//这就是dealer与req的不同之处，req在此处会自动加入一个空帧
		LOGGER.info("I:sending reply to broker\n" + msg.toString());
		//msg.dump(log.out());
		msg.send(worker);
		msg.destroy();
	}
}