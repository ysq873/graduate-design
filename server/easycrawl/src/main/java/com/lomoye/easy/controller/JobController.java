package com.lomoye.easy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Preconditions;
import com.jfinal.plugin.activerecord.Db;
import com.lomoye.easy.backend.JobStatsHolder;
import com.lomoye.easy.backend.SpiderHolder;
import com.lomoye.easy.constants.JobStatus;
import com.lomoye.easy.domain.ConfigurableSpider;
import com.lomoye.easy.domain.Job;
import com.lomoye.easy.exception.BusinessException;
import com.lomoye.easy.exception.ErrorCode;
import com.lomoye.easy.model.JobModel;
import com.lomoye.easy.model.JobStatModel;
import com.lomoye.easy.model.common.ResultData;
import com.lomoye.easy.model.common.ResultPagedList;
import com.lomoye.easy.model.search.JobSearchModel;
import com.lomoye.easy.service.ConfigurableSpiderService;
import com.lomoye.easy.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import us.codecraft.webmagic.Spider;

import java.util.List;

/**
 * 2019/9/1 19:22
 * yechangjun
 */
@Slf4j
@RestController
@RequestMapping("/job")
@Api(tags = "爬虫任务", description = "爬虫任务 lomoye")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ConfigurableSpiderService configurableSpiderService;

    @PostMapping
    @ApiOperation("增加任务")
    @ResponseBody
    public ResultData<Job> addJob(@RequestBody JobModel jobModel) {
        Preconditions.checkArgument(jobModel != null);
        if (jobModel.getConfigurableSpiderId() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "爬虫id不能为空");
        }

        ConfigurableSpider spider = configurableSpiderService.getById(jobModel.getConfigurableSpiderId());
        if (spider == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "爬虫不存在");
        }

        Job job = Job.valueOf(spider);
        jobService.save(job);
        return new ResultData<>(job);
    }

    @PostMapping("/stop/{id}")
    @ApiOperation("停止任务")
    @ResponseBody
    public ResultData<Job> stopJob(@PathVariable String id) {
        Preconditions.checkArgument(id != null);
        Job job = jobService.getById(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "任务不存在");
        }
        job.setStatus(JobStatus.SUCCESS);
        jobService.updateById(job);

        List<Spider> spiders = SpiderHolder.getSpider(id);
        if (CollectionUtils.isNotEmpty(spiders)) {
            spiders.forEach(
                    Spider::stop
            );

            SpiderHolder.removeSpider(id);
        }

        return new ResultData<>(job);
    }

    @PostMapping("/pause/{id}")
    @ApiOperation("暂停任务")
    @ResponseBody
    public ResultData<Job> pauseJob(@PathVariable String id) {
        Preconditions.checkArgument(id != null);
        Job job = jobService.getById(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "任务不存在");
        }
        job.setStatus(JobStatus.PAUSED);
        jobService.updateById(job);

        List<Spider> spiders = SpiderHolder.getSpider(id);
        if (CollectionUtils.isNotEmpty(spiders)) {
            spiders.forEach(
                    Spider::stop
            );

            SpiderHolder.removeSpider(id);
        }
        return new ResultData<>(job);
    }

    @PostMapping("/restart/{id}")
    @ApiOperation("重启任务")
    @ResponseBody
    public ResultData<Job> restartJob(@PathVariable String id) {
        Preconditions.checkArgument(id != null);
        Job job = jobService.getById(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "任务不存在");
        }
        //重新加入后台调度
        job.setStatus(JobStatus.WAIT);
        jobService.updateById(job);
        return new ResultData<>(job);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询单个任务")
    @ResponseBody
    public ResultData<Job> getJob(@PathVariable String id) {
        Preconditions.checkArgument(id != null);

        return new ResultData<>(jobService.getById(id));
    }

    @PostMapping("/search")
    @ApiOperation("分页查询")
    @ResponseBody
    public ResultPagedList<Job> searchJob(@RequestBody JobSearchModel searchModel) {
        IPage<Job> page = new Page<>(searchModel.getPageNo(), searchModel.getPageSize());
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(Strings.isNotEmpty(searchModel.getSpiderName()), Job::getSpiderName, searchModel.getSpiderName())
                .like(Strings.isNotEmpty(searchModel.getSpiderTableName()), Job::getSpiderTableName, searchModel.getSpiderTableName())
                .orderByDesc(Job::getCreateTime);

        page = jobService.page(page, queryWrapper);
        List<Job> jobs = page.getRecords();
        if (CollectionUtils.isNotEmpty(jobs)) {
            for (Job job : jobs) {
                Long count = 0L;
                try {
                    count  = Db.queryLong("select count(*) from " + job.getSpiderTableName() + " where job_uuid=?", job.getId());
                } catch (Exception e) {
                    //动态表可能还没创建
                    log.debug("searchJob query job count error", e);
                }

                job.setSuccessNum(count);
            }
        }
        return new ResultPagedList<>(page.getRecords(), page.getTotal(), searchModel);
    }

    @GetMapping("/get-stat-info/{id}")
    @ApiOperation("查询统计信息")
    @ResponseBody
    public ResultData<JobStatModel> getStatInfo(@PathVariable String id) {
        Preconditions.checkArgument(Strings.isNotBlank(id));

        JobStatModel stat = JobStatsHolder.getJobStat(id);

        return new ResultData<>(stat);
    }
}