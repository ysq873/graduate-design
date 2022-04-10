package com.lomoye.easy.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/8/28 14:56
 * yechangjun
 * 列表模式
 */
@Data
public class SpiderSettingModel {
    @ApiModelProperty(notes = "爬虫编号")
    private String id;

    //爬虫配置项
    @ApiModelProperty(notes = "线程数")
    private Integer threadNum;

    @ApiModelProperty(notes = "页面处理完后的睡眠时间 单位秒")
    private Integer sleepTime;

    @ApiModelProperty(notes = "页面下载失败重试次数")
    private Integer retryTimes;

    @ApiModelProperty(notes = "重试睡眠时间 单位秒")
    private Integer retrySleepTime;

    @ApiModelProperty(notes = "页面爬取失败后放回队列的次数")
    private Integer cycleRetryTimes;

    @ApiModelProperty(notes = "下载页面超时时间 单位秒")
    private Integer timeOut;
}
