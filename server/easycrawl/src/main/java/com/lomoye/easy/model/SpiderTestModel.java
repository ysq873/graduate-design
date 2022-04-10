package com.lomoye.easy.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2020/9/6 16:55
 * yechangjun
 */
@Data
public class SpiderTestModel {
    @ApiModelProperty(notes = "网址")
    private String url;

    @ApiModelProperty(notes = "正则")
    private String regex;

    @ApiModelProperty(notes = "xpath")
    private String xpath;

    @ApiModelProperty(notes = "页面爬取是否是用动态抓取 默认0启用 1.启用")
    private Integer isDynamic;

    @ApiModelProperty(notes = "联想xpath的文字")
    private String content;
}
