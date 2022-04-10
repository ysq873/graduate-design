package com.lomoye.easy.backend;

import com.jfinal.plugin.activerecord.Db;
import com.lomoye.easy.constants.JobStatus;
import com.lomoye.easy.domain.Job;
import com.lomoye.easy.model.JobStatModel;
import com.lomoye.easy.service.JobService;
import com.lomoye.easy.utils.LocalDateUtil;
import com.lomoye.easy.utils.SerializationUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 2020/7/24 12:02
 * yechangjun
 */
@Slf4j
public class JobStater implements Runnable {

    //统计间隔
    private static final Long STAT_INTERVAL = 5L;

    //最多出现10次异常
    private static final Long MAX_NODE_NUM = 12L;

    //最多出现10次异常
    private static final Long MAX_ERROR_COUNT = 10L;

    private Job job;
    private JobService jobService;

    public JobStater(Job job, JobService jobService) {
        this.job = job;
        this.jobService = jobService;
    }


    @Override
    public void run() {
        //异常次数
        long errorCount = 0L;
        while (true) {
            try {
                //检查一下爬虫暂停 结束了没有
                Job selected = jobService.getById(job.getId());
                if (selected == null || !Objects.equals(selected.getStatus(), JobStatus.RUNING)) {
                    log.info("job not running so break|job={}", SerializationUtil.obj2String(job));
                    break;
                }

                JobStatModel model = JobStatsHolder.getJobStat(job.getId());
                if (model == null) {
                    log.info("stat model not exist create one job id={}", job.getId());
                    model = JobStatModel.getInstance(job);
                    JobStatsHolder.putJobStat(model);
                }
                LocalDateTime endTime = LocalDateTime.now();
                LocalDateTime startTime = endTime.minusSeconds(STAT_INTERVAL);

                long count = Db.queryLong("select count(*) from " + job.getSpiderTableName() + " where job_uuid=? and create_time >= ? and create_time < ?", job.getId(), LocalDateUtil.getDateTimeAsString(startTime), LocalDateUtil.getDateTimeAsString(endTime));
                model.getNodes().add(new JobStatModel.Node(count, LocalDateUtil.getDateTimeAsString(endTime)));
                if (model.getNodes().size() > MAX_NODE_NUM) {
                    model.getNodes().remove(0);
                }

                Thread.sleep(STAT_INTERVAL * 1000L);
            } catch (Exception e) {
                //动态表可能还没创建
                log.debug("searchJob query job count error", e);
                errorCount++;
                if (errorCount > MAX_ERROR_COUNT) {
                    log.info("searchJob query job count error too much count break", e);
                    break;
                }
                try {
                    Thread.sleep(STAT_INTERVAL * 1000L);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }

        //统计完了删除内存中的统计数据
        JobStatsHolder.removeJobStat(job.getId());
    }
}
