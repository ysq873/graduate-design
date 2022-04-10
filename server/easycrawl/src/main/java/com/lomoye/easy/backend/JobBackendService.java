package com.lomoye.easy.backend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lomoye.easy.ListPatternProcessor;
import com.lomoye.easy.component.DefaultHttpClientDownloader;
import com.lomoye.easy.component.DownloaderFactory;
import com.lomoye.easy.constants.CoreConstant;
import com.lomoye.easy.constants.JobStatus;
import com.lomoye.easy.domain.ConfigurableSpider;
import com.lomoye.easy.domain.Job;
import com.lomoye.easy.domain.ProxyChannel;
import com.lomoye.easy.proxy.ProxyProviderFactory;
import com.lomoye.easy.service.ConfigurableSpiderService;
import com.lomoye.easy.service.JobService;
import com.lomoye.easy.service.ProxyChannelService;
import com.lomoye.easy.utils.LinkUtil;
import com.lomoye.easy.utils.LocalDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 2019/9/11 16:19
 * yechangjun
 */
@Slf4j
@Service
public class JobBackendService extends Thread {

    /**
     * 爬虫链接和进度持久化文件夹
     */
    @Value("${spider.file-cache-queue-dir}")
    private String fileCacheQueueDir;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProxyChannelService proxyChannelService;

    @Autowired
    private ConfigurableSpiderService configurableSpiderService;

    /**
     * 运行爬虫任务线程池
     */
    private ExecutorService spiderExecutor = new ThreadPoolExecutor(8, 8, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("spider-pool-%d").build(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 统计爬虫线程池
     */
    private ExecutorService statExecutor = new ThreadPoolExecutor(8, 8, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("stat-spider-pool-%d").build(), new ThreadPoolExecutor.AbortPolicy());


    @Override
    public void run() {
        //重启将进行中的任务设置为等待中
        jobService.updateRunningStatusToWaitStatus();
        while (true) {
            QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
            List<Job> jobs = jobService.list(queryWrapper.lambda().eq(Job::getStatus, JobStatus.WAIT));
            if (CollectionUtils.isEmpty(jobs)) {
                try {
                    log.info("JobBackendService empty job sleep 1s");
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    log.info("job backend sleep error", e);
                }

                continue;
            }
            for (final Job job : jobs) {
                log.info("job queuing uuid={}", job.getId());
                job.setStatus(JobStatus.QUEUING);
                job.setStartTime(LocalDateTime.now());
                job.setModifyTime(LocalDateTime.now());
                jobService.updateById(job);

                spiderExecutor.submit(() -> runJob(job));
            }
        }
    }

    private void runJob(final Job job) {
        log.info("job start run uuid={}", job.getId());
        //跟新任务状态为运行中
        updateJobStatusAsRunning(job);
        ConfigurableSpider spider = configurableSpiderService.getById(job.getSpiderId());
        //解析并设置spider需要爬取的字段
        setSpiderFields(spider);

        ListPatternProcessor processor = new ListPatternProcessor();
        processor.setMetaInfo(spider);
        Map<String/*内容页链接*/, LinkedHashMap<String, String> /*列表页提取的数据*/> itemHolder = new ConcurrentHashMap<>();

        //构建正文页爬虫
        Spider contentWorker = buildContentWorker(spider, job, itemHolder, processor);
        //构建列表页爬虫
        Spider spiderWorker = buildListSpiderWorker(contentWorker, spider, job, itemHolder, processor);
        //将爬虫保存起来 用于控制其生命周期 比如停止爬虫
        SpiderHolder.putSpider(spiderWorker, contentWorker);

        //异步启动正文页和内容页的爬虫
        spiderWorker.runAsync();
        contentWorker.runAsync();

        //统计线程开启 统计任务爬取速度
        startStat(job);

        //阻塞当前线程 直到列表页爬虫没有任务
        blockCurrentThread(spiderWorker);

        //列表页爬虫停止了，唤醒通知可能休眠的内容页爬虫
        wakeupContentSpider(contentWorker);

        //阻塞当前线程 直到内容页爬虫没有任务
        blockCurrentThread(contentWorker);

        //爬虫容器移除uuid对应的爬虫
        SpiderHolder.removeSpider(spiderWorker.getUUID());

        //任务完成 跟新job
        finishJob(job);
    }

    private void finishJob(Job job) {
        Job selectedJob = jobService.getById(job.getId());
        if (selectedJob == null) {
            return;
        }
        //暂停状态返回
        if (Objects.equals(selectedJob.getStatus(), JobStatus.PAUSED)) {
            log.info("job paused");
            return;
        }

        selectedJob.setStatus(JobStatus.SUCCESS);
        selectedJob.setEndTime(LocalDateTime.now());
        selectedJob.setTimeCost(LocalDateUtil.getSecondInterval(selectedJob.getStartTime(), selectedJob.getEndTime()));
        jobService.updateById(selectedJob);
        log.info("job end run uuid={}", job.getId());
    }

    private void wakeupContentSpider(Spider contentWorker) {
        //内容页爬虫设置没有新任务时退出
        contentWorker.setExitWhenComplete(true);
        //不添加链接 间接唤醒线程
        contentWorker.addUrl();
    }

    private void blockCurrentThread(Spider spiderWorker) {
        while (spiderWorker.getStatus() != Spider.Status.Stopped) {
            try {
                sleep(1000);
                log.info("spiderWorker running sleep");
            } catch (InterruptedException e) {
                log.info("spiderWorker interruptedException", e);
            }
        }

        log.info("spiderWorker stoped|uuid={}", spiderWorker.getUUID());
    }

    private void startStat(Job job) {
        statExecutor.submit(new JobStater(job, jobService));
    }


    private Spider buildListSpiderWorker(Spider contentWorker, ConfigurableSpider spider, Job job, Map<String, LinkedHashMap<String, String>> itemHolder, ListPatternProcessor processor) {
        Spider spiderWorker = Spider.create(processor);
        spiderWorker.setDownloader(DownloaderFactory.getDownloader(spider.getIsDynamic()));
        setSpiderProxy(spiderWorker, spider);
        spiderWorker.thread(spider.getThreadNum() == null ? 1 : spider.getThreadNum()).setUUID(job.getId()).addUrl(spider.getEntryUrl()).
                addPipeline((resultItems, task) -> {
                    String requestUrl = resultItems.getRequest().getUrl();
                    log.info("get list page|url={}|uuid={}|spiderId={}", requestUrl, job.getId(), job.getSpiderId());

                    final List<LinkedHashMap<String, String>> resultList = new ArrayList<>();
                    Map<String, Object> resultMap = resultItems.getAll();
                    resultMap.forEach((k, v) -> {
                        List<String> vstr = (ArrayList<String>) v;
                        if (resultList.isEmpty()) {
                            for (int i = 0; i < vstr.size(); i++) {
                                resultList.add(new LinkedHashMap<>());
                            }
                        }
                        for (int i = 0; i < resultList.size(); i++) {
                            Map<String, String> obj = resultList.get(i);
                            obj.put(k, vstr.get(i));
                        }
                    });

                    //判断有没有正文页
                    if (Strings.isNullOrEmpty(spider.getContentXpath())) {
                        //没有正文页面 直接保存数据
                        configurableSpiderService.saveData(resultList, spider, task.getUUID());
                    } else {
                        //如果有正文页面 先把数据存放到内存里 等待后续处理完正文页后再一起保存
                        resultList.forEach((v) -> {
                            String url = v.get(CoreConstant.PAGE_URL);
                            if (!Strings.isNullOrEmpty(url)) {

                                //把正文页的链接添加到带爬取页面
                                url = LinkUtil.getAbsoluteURL(requestUrl, url);

                                itemHolder.put(url, v);
                                contentWorker.addUrl(url);
                            }
                        });
                    }
                }).setScheduler(new FileCacheQueueScheduler(System.getProperty("user.home") + "/" + fileCacheQueueDir));

        return spiderWorker;
    }

    private Spider buildContentWorker(ConfigurableSpider spider, Job job, Map<String, LinkedHashMap<String, String>> itemHolder, ListPatternProcessor processor) {
        Spider contentWorker = Spider.create(processor);
        contentWorker.setDownloader(DownloaderFactory.getDownloader(spider.getIsDynamic()));
        setSpiderProxy(contentWorker, spider);
        if (!Strings.isNullOrEmpty(spider.getContentXpath())) {
            contentWorker.thread(spider.getThreadNum() == null ? 1 : spider.getThreadNum()).setUUID(job.getId()).
                    addPipeline((resultItems, task) -> {
                        String requestUrl = resultItems.getRequest().getUrl();
                        log.info("get content page|url={}|uuid={}|spiderId={}", requestUrl, job.getId(), job.getSpiderId());

                        Map<String, Object> resultMap = resultItems.getAll();

                        //合并数据
                        LinkedHashMap<String, String> item = itemHolder.get(requestUrl);
                        //把暂存的数据删了 防止内存溢出
                        itemHolder.remove(requestUrl);

                        resultMap.forEach((k, v) -> item.put(k, ((List) v).get(0).toString()));
                        //保存
                        final List<LinkedHashMap<String, String>> resultList = new ArrayList<>();
                        resultList.add(item);
                        configurableSpiderService.saveData(resultList, spider, task.getUUID());

                    }).setScheduler(new FileCacheQueueScheduler(System.getProperty("user.home") + "/" + fileCacheQueueDir + "/content"));

        }
        contentWorker.setExitWhenComplete(false);
        return contentWorker;
    }

    private void setSpiderFields(ConfigurableSpider spider) {
        spider.setFields(configurableSpiderService.parseFields(spider.getFieldsJson()));
        if (!Strings.isNullOrEmpty(spider.getContentXpath()) && !Strings.isNullOrEmpty(spider.getContentFieldsJson())) {
            spider.setContentFields(configurableSpiderService.parseFields(spider.getContentFieldsJson()));
            spider.getFields().put(CoreConstant.PAGE_URL, spider.getContentXpath());
        }
    }

    private void updateJobStatusAsRunning(Job job) {
        job.setStatus(JobStatus.RUNING);
        job.setStartTime(LocalDateTime.now());
        job.setModifyTime(LocalDateTime.now());
        jobService.updateById(job);
    }

    /**
     * 设置爬虫代理 TODO 动态抓取不支持代理
     */
    private void setSpiderProxy(Spider worker, ConfigurableSpider spider) {
        if (Strings.isNullOrEmpty(spider.getProxyChannelId())) {
            return;
        }

        ProxyChannel proxyChannel = proxyChannelService.getById(spider.getProxyChannelId());

        ProxyProvider proxyProvider = ProxyProviderFactory.build(proxyChannel);

        DefaultHttpClientDownloader httpClientDownloader = new DefaultHttpClientDownloader();
        httpClientDownloader.setProxyProvider(proxyProvider);

        worker.setDownloader(httpClientDownloader);
    }
}
