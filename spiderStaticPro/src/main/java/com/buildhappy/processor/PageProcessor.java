package com.buildhappy.processor;

import java.util.List;

import com.buildhappy.Page;
import com.buildhappy.Site;
import com.buildhappy.pipeline.mybatis.domain.Apk;

/**
 * Interface to be implemented to customize a crawler.<br>
 * <br>
 * In PageProcessor, you can customize:
 * <p/>
 * start urls and other settings in {@link Site}<br>
 * how the urls to fetch are detected               <br>
 * how the data are extracted and stored             <br>
 *
 * @author code4crafter@gmail.com <br>
 * @see Site
 * @see Page
 * @since 0.1.0
 */
public interface PageProcessor {

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param page
     */
    public Apk process(Page page);
    
    /**
     * process the page, extract urls to fetch, extract the data and store
     * one page contains multi apps in some sites,return the apps through the list container 
     *
     * @param page
     */
    public List<Apk> processMulti(Page page);

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    public Site getSite();
}
