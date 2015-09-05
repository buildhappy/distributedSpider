package com.buildhappy.scheduler.imp;

import org.apache.http.annotation.ThreadSafe;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.scheduler.DuplicateRemovedScheduler;
import com.buildhappy.scheduler.MonitorableScheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Basic Scheduler implementation.<br>
 * Store urls to fetch in LinkedBlockingQueue and remove duplicate urls by HashMap.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@ThreadSafe
public class QueueScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler {
	
    private BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();

    @Override
    public void pushWhenNoDuplicate(Request request, Task task) {
        queue.add(request);
    }

    //@Override
    public synchronized Request poll(Task task) {
        return queue.poll();
    }

    //@Override
    public int getLeftRequestsCount(Task task) {
        return queue.size();
    }

    //@Override
    public int getTotalRequestsCount(Task task) {
        return getDuplicateRemover().getTotalRequestsCount(task);
    }
    
    public static void main(String[] args){
    	QueueScheduler queue = new QueueScheduler();
    	queue.push(new Request("tstst"), null);
    	System.out.println(queue.poll(null).getUrl());
    }
}
