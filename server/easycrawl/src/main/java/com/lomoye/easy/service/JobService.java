package com.lomoye.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lomoye.easy.domain.Job;

/**
 * 2019/9/1 19:15
 * yechangjun
 */
public interface JobService extends IService<Job> {
    /**
     * 将运行中的状态重置为待运行状态
     * @return
     */
    boolean updateRunningStatusToWaitStatus();
}
