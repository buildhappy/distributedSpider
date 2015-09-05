package com.buildhappy.scheduler.imp;

import com.buildhappy.Request;
import com.buildhappy.Task;
import com.buildhappy.scheduler.DuplicateRemover;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放已经处理的url
 * @author code4crafer@gmail.com
 */
public class HashSetDuplicateRemover implements DuplicateRemover {
	
    private Set<String> urls = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    //@Override
    public boolean isDuplicate(Request request, Task task) {
        return !urls.add(getUrl(request));
    }

    protected String getUrl(Request request) {
        return request.getUrl();
    }

   // @Override
    public void resetDuplicateCheck(Task task) {
        urls.clear();
    }

    //@Override
    public int getTotalRequestsCount(Task task) {
        return urls.size();
    }
}
