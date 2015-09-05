package com.buildhappy.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.scheduler.imp.HashSetDuplicateRemover;

/**
 * Remove duplicate urls and only push urls which are not duplicate.<br></br>
 *
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
public abstract class DuplicateRemovedScheduler implements Scheduler {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private String siteRoot = null;
    
    private DuplicateRemover duplicatedRemover = new HashSetDuplicateRemover();
    
    public DuplicateRemover setSiteRoot(String siteRoot) {
    	this.siteRoot = siteRoot;
        return duplicatedRemover;
    }
    
    public DuplicateRemover getDuplicateRemover() {
        return duplicatedRemover;
    }

    public DuplicateRemovedScheduler setDuplicateRemover(DuplicateRemover duplicatedRemover) {
        this.duplicatedRemover = duplicatedRemover;
        return this;
    }

    //@Override
    public void push(Request request, Task task) {
    	//logger.info("get a candidate url {}", request.getUrl());
        logger.trace("get a candidate url {}", request.getUrl());
        if (!duplicatedRemover.isDuplicate(request, task) || shouldReserved(request)) {
            logger.debug("push to queue {}", request.getUrl());
            pushWhenNoDuplicate(request, task);
        }
    }

    protected boolean shouldReserved(Request request) {
        return request.getExtra(Request.CYCLE_TRIED_TIMES) != null;
    }

    protected void pushWhenNoDuplicate(Request request, Task task) {
    	//在子类中实现，比如QueueScheduler。
    }
}
