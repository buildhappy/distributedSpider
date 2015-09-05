package com.buildhappy;

import com.buildhappy.downloader.Downloader;
import com.buildhappy.downloader.HttpClientDownloader;
import com.buildhappy.pipeline.ConsolePipeline;
import com.buildhappy.pipeline.Pipeline;
import com.buildhappy.pipeline.mybatis.domain.Apk;
import com.buildhappy.processor.PageProcessor;
import com.buildhappy.redis.RedisToolForSpider;
import com.buildhappy.selector.thread.CountableThreadPool;
import com.buildhappy.utils.UrlUtils;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class StaticProSpider implements Runnable, Task {
    protected Downloader downloader;

    protected PageProcessor pageProcessor;
    
    protected List<Pipeline> pipelines = new ArrayList<Pipeline>();
    //protected List<Pipeline> pipelines = new ArrayList<Pipeline>();
    
    protected List<Request> startRequests;

    protected Site site;

    protected String uuid;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected CountableThreadPool threadPool;

    protected ExecutorService executorService;//线程池

    protected int threadNum = 3;

    protected AtomicInteger stat = new AtomicInteger(STAT_INIT);//线程安全的

    protected boolean exitWhenComplete = true;

    protected final static int STAT_INIT = 0;

    protected final static int STAT_RUNNING = 1;

    protected final static int STAT_STOPPED = 2;
    
    protected boolean spawnUrl = true;

    protected boolean destroyWhenExit = true;

    private ReentrantLock newUrlLock = new ReentrantLock();

    private Condition newUrlCondition = newUrlLock.newCondition();

    private List<SpiderListener> spiderListeners;

    private final AtomicLong pageCount = new AtomicLong(0);

    private Date startTime;

    private int emptySleepTime = 30000;
    
    private final String siteRoot;
    
    private final RedisToolForSpider redisTool;
    
    private final static String packageName = "com.buildhappy.processor.imp.";
    /**
     * container for app information
     * ToDo 将该数据放在HBase中
     */
    private BlockingQueue<Apk> appsInfo;
    
    public static StaticProSpider create(PageProcessor pageProcessor , String siteRoot) {
        return new StaticProSpider(pageProcessor , siteRoot);
    }
    
    public static StaticProSpider create(String pageProcessorName , String siteRoot) throws Exception {
    	if(!pageProcessorName.contains(packageName)){
    		pageProcessorName = packageName + pageProcessorName;
    	}
    	PageProcessor pagePro = dynamicCreateObjByName(pageProcessorName);
        return create( pagePro, siteRoot);
    }
    
    /**
     * create a spider with pageProcessor,taskId and channelId
     */
    public StaticProSpider(PageProcessor pageProcessor , String siteRoot){
        this.pageProcessor = pageProcessor;
        this.site = pageProcessor.getSite();
        this.startRequests = pageProcessor.getSite().getStartRequests();
        
    	this.siteRoot = siteRoot;
    	redisTool = new RedisToolForSpider(siteRoot);
    	redisTool.storeToDoRequest(new Request(siteRoot), this);
    }

    /**
     * Set startUrls of Spider.<br>
     * Prior to startUrls of Site.
     */
    public StaticProSpider startUrls(List<String> startUrls) {
        checkIfRunning();
        this.startRequests = UrlUtils.convertToRequests(startUrls);
        return this;
    }

    /**
     * Set startUrls of Spider.<br>
     * Prior to startUrls of Site.
     */
    public StaticProSpider startRequest(List<Request> startRequests) {
        checkIfRunning();
        this.startRequests = startRequests;
        return this;
    }

    /**
     * Set an uuid for spider.<br>
     * Default uuid is domain of site.<br>
     */
    public StaticProSpider setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }
    /**
     * add a pipeline for Spider
     */
    public StaticProSpider addPipeline(Pipeline pipeline) {
        checkIfRunning();
        this.pipelines.add(pipeline);
        return this;
    }
    
    /**
     * set pipelines for Spider
     */
    public StaticProSpider setPipelines(List<Pipeline> pipelines) {
        checkIfRunning();
        this.pipelines = pipelines;
        return this;
    }

    /**
     * set the downloader of spider
     */
    public StaticProSpider setDownloader(Downloader downloader) {
        checkIfRunning();
        this.downloader = downloader;
        return this;
    }

    protected void initComponent() {
        if (downloader == null) {
            this.downloader = new HttpClientDownloader();//默认使用的Downloader
        }
        if (pipelines.isEmpty()) {
            pipelines.add(new ConsolePipeline());
        }
        downloader.setThread(threadNum);//pageDownloader
        if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new CountableThreadPool(threadNum, executorService);
            } else {
                threadPool = new CountableThreadPool(threadNum);
            }
        }
        if (startRequests != null) {
            for (Request request : startRequests) {
                //storeToDoRequest(request , this);
                redisTool.storeToDoRequest(request, this);
            }
            startRequests.clear();
        }
        startTime = new Date();
    }
    
    public void run() {
        checkRunningStat();
        initComponent();
        logger.info("Spider " + getUUID() + " started!");
        while (!Thread.currentThread().isInterrupted() && stat.get() == STAT_RUNNING) {
//            Request request = scheduler.poll(this);
            //Request request = getToDoRequest(this);
        	Request request = redisTool.getToDoRequest(this);
            if (request == null) {
                if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                    break;
                }
                // wait until new url added
                waitNewUrl();
            } else {
                final Request requestFinal = request;
                threadPool.execute(new Runnable() {
                    public void run() {
                        try {
                            processRequest(requestFinal);
                            onSuccess(requestFinal);
                        } catch (Exception e) {
                        	logger.error(e.toString());
                            onError(requestFinal);
                            logger.error("process request " + requestFinal + " error", e);
                        } finally {
                            /*if (site.getHttpProxyPool().isEnable()) {
                                site.returnHttpProxyToPool((HttpHost) requestFinal.getExtra(Request.PROXY), (Integer) requestFinal
                                        .getExtra(Request.STATUS_CODE));
                            }*/
                            pageCount.incrementAndGet();
                            signalNewUrl();
                        }
                    }
                });
            }
        }
        stat.set(STAT_STOPPED);
        try {
			sendData();
		} catch (UnknownHostException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
        // release some resources
        if (destroyWhenExit) {
            close();
        }
    }
    /**
     * clear the pipelines set
     */
    public StaticProSpider clearPipeline() {
        pipelines = new ArrayList<Pipeline>();
        return this;
    }
    protected void onError(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onError(request);
            }
        }
    }

    protected void onSuccess(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onSuccess(request);
            }
        }
    }

    private void checkRunningStat() {
        while (true) {
            int statNow = stat.get();
            if (statNow == STAT_RUNNING) {
                throw new IllegalStateException("Spider is already running!");
            }
            if (stat.compareAndSet(statNow, STAT_RUNNING)) {
                break;
            }
        }
    }

    /**
     * the crawl task is finished and send the results
     */
    public void close() {
    	logger.info("Spider close()");
        destroyEach(downloader);
        destroyEach(pageProcessor);
        for (Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
        threadPool.shutdown();
    }
    
    /**
     * send data the the remote user
     * @throws UnknownHostException 
     */
    public void sendData() throws UnknownHostException{
    	logger.info("Spider sendData()");
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
            	logger.error(e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Process specific urls without url discovering.
     */
    public void test(String... urls) {
        initComponent();
        if (urls.length > 0) {
            for (String url : urls) {
                processRequest(new Request(url));
            }
        }
    }

    protected void processRequest(Request request) {
    	logger.info(Thread.currentThread().getName() + " processRequest(Request):" + request.getUrl());
    	Page page = downloader.download(request, this);
        if (page == null) {
            sleep(site.getSleepTime());
            onError(request);
            return;
        }
        // for cycle retry
        if (page.isNeedCycleRetry()) {
            extractAndAddRequests(page, true);
            sleep(site.getSleepTime());
            return;
        }
        pageProcessor.process(page);
        extractAndAddRequests(page, spawnUrl);
        if (!page.getResultItems().isSkip()) {
            for (Pipeline pipeline : pipelines) {
                pipeline.process(page.getResultItems(), this);
            }
        }
        sleep(site.getSleepTime());
    }

    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //将page中的targetRequest添加到Scheduler中
    protected void extractAndAddRequests(Page page, boolean spawnUrl) {
        if (spawnUrl && CollectionUtils.isNotEmpty(page.getTargetRequests())) {
            for (Request request : page.getTargetRequests()) {
                addRequest(request);
            }
        }
    }

    private void addRequest(Request request) {
        if (site.getDomain() == null && request != null && request.getUrl() != null) {
            site.setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        redisTool.storeToDoRequest(request , this);
    }

    protected void checkIfRunning() {
        if (stat.get() == STAT_RUNNING) {
            throw new IllegalStateException("Spider is already running!");
        }
    }

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    /**
     * Add urls to crawl.
     */
    public StaticProSpider addUrl(String... urls) {
        for (String url : urls) {
            addRequest(new Request(url));
        }
        signalNewUrl();
        return this;
    }


    /**
     * Add urls with information to crawl.<br/>
     */
    public StaticProSpider addRequest(Request... requests) {
        for (Request request : requests) {
            addRequest(request);
        }
        signalNewUrl();
        return this;
    }

    private void waitNewUrl() {
    	logger.info("Spider waitNewUrl()");
        newUrlLock.lock();
        try {
            //double check
            if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                return;
            }
            newUrlCondition.await(emptySleepTime , TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    public void start() {
        runAsync();
    }

    public void stop() {
        if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
            logger.info("Spider " + getUUID() + " stop success!");
        } else {
            logger.info("Spider " + getUUID() + " stop fail!");
        }
    }
    
    
	/**
	 * 通过类名创建类的对象
	 */
	private static PageProcessor dynamicCreateObjByName(String name)
			throws Exception {
		Class c;
		PageProcessor o;
		c = Class.forName(name);
		o = (PageProcessor) (c.getClassLoader().loadClass(name)).newInstance();
		return o;
	}
	
	
    /**
     * start with more than one threads
     */
    public StaticProSpider thread(int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    /**
     * start with more than one threads
     */
    public StaticProSpider thread(ExecutorService executorService, int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    public boolean isExitWhenComplete() {
        return exitWhenComplete;
    }

    /**
     * Exit when complete.
     * True: exit when all url of the site is downloaded.
     * False: not exit until call stop() manually.
     */
    public StaticProSpider setExitWhenComplete(boolean exitWhenComplete) {
        this.exitWhenComplete = exitWhenComplete;
        return this;
    }

    public boolean isSpawnUrl() {
        return spawnUrl;
    }

    /**
     * Get page count downloaded by spider.
     */
    public long getPageCount() {
        return pageCount.get();
    }

    /**
     * Get running status by spider.
     */
    public Status getStatus() {
        return Status.fromValue(stat.get());
    }
    
    public enum Status {
        Init(0), Running(1), Stopped(2);

        private Status(int value) {
            this.value = value;
        }

        private int value;

        int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            return Init;
        }
    }

    /**
     * Get thread count which is running
     */
    public int getThreadAlive() {
        if (threadPool == null) {
            return 0;
        }
        return threadPool.getThreadAlive();
    }

    /**
     * Whether add urls extracted to download.<br>
     * Add urls to download when it is true, and just download seed urls when it is false. <br>
     * DO NOT set it unless you know what it means!
     */
    public StaticProSpider setSpawnUrl(boolean spawnUrl) {
        this.spawnUrl = spawnUrl;
        return this;
    }

    public String getUUID() {
        if (uuid != null) {
            return uuid;
        }
        if (site != null) {
            return site.getDomain();
        }
        uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public StaticProSpider setExecutorService(ExecutorService executorService) {
        checkIfRunning();
        this.executorService = executorService;
        return this;
    }

    public Site getSite() {
        return site;
    }

    public List<SpiderListener> getSpiderListeners() {
        return spiderListeners;
    }

    public StaticProSpider setSpiderListeners(List<SpiderListener> spiderListeners) {
        this.spiderListeners = spiderListeners;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    /**
     * Set wait time when no url is polled.<br></br>
     */
    public void setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
    }
	
    public static void main(String[] args) throws Exception{
    	PageProcessor pagePro = dynamicCreateObjByName("com.buildhappy.processor.imp.PagePro360");
    	StaticProSpider spider = StaticProSpider.create(pagePro , "http://zhushou.360.cn/").addUrl("http://zhushou.360.cn/");
    	spider.start();
    }
}
