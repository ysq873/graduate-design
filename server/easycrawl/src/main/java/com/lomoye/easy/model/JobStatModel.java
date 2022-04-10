package com.lomoye.easy.model;

import com.lomoye.easy.domain.Job;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020/7/24 11:38
 * yechangjun
 * 爬虫统计信息
 */
@Data
public class JobStatModel {

    @ApiModelProperty(notes = "任务id")
    private String jobId;

    @ApiModelProperty(notes = "爬取速度（每秒爬取几个，5秒统计一次，保存最近12次的数据）")
    private List<Node> nodes = new ArrayList<>();

    public static JobStatModel getInstance(Job job) {
        JobStatModel jobStatModel = new JobStatModel();
        jobStatModel.setJobId(job.getId());
        return jobStatModel;
    }

    @Data
    public static class Node {
        public Node(Long count, String time) {
            this.count = count;
            this.time = time;
        }

        Long count;

        String time;

    }
}
