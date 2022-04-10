package com.lomoye.easy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lomoye.easy.constants.JobStatus;
import com.lomoye.easy.dao.JobMapper;
import com.lomoye.easy.domain.Job;
import com.lomoye.easy.service.JobService;
import org.springframework.stereotype.Service;

/**
 * 2019/9/1 19:19
 * yechangjun
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Override
    public boolean updateRunningStatusToWaitStatus() {
        Job condition = new Job();
        condition.setStatus(JobStatus.WAIT);

        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        return update(condition, queryWrapper.in("status", JobStatus.RUNING, JobStatus.QUEUING));
    }
}
