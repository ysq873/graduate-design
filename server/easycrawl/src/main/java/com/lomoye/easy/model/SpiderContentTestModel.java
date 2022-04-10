package com.lomoye.easy.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2020/9/6 16:55
 * yechangjun
 * 正文页字段测试爬取
 */
@Data
public class SpiderContentTestModel {
    @ApiModelProperty(notes = "入口页网址")
    private String entryUrl;

    @ApiModelProperty(notes = "正文页xpath")
    private String contentXpath;

    @ApiModelProperty(notes = "字段xpath")
    private String xpath;

    @ApiModelProperty(notes = "页面爬取是否是用动态抓取 默认0启用 1.启用")
    private Integer isDynamic;
}
