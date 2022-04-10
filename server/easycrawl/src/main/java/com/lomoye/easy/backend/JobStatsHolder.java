package com.lomoye.easy.backend;

import com.lomoye.easy.model.JobStatModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 2020/4/23 17:50
 * yechangjun
 */
public class JobStatsHolder {

    //正在运行的爬虫对象的容器
    private static Map<String/*jobId*/, JobStatModel> jobStatMap = new ConcurrentHashMap<>();

    //放入任务统计
    public static void putJobStat(JobStatModel jobStatModel) {
        jobStatMap.put(jobStatModel.getJobId(), jobStatModel);
    }

    //获取任务统计
    public static JobStatModel getJobStat(String jobId) {
        return jobStatMap.get(jobId);
    }

    //删除任务统计
    public static void removeJobStat(String jobId) {
        jobStatMap.remove(jobId);
    }

}
