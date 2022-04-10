package com.lomoye.easy.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/8/28 14:56
 * yechangjun
 * 列表模式
 */
@Data
public class ListPatternModel {
    @ApiModelProperty(notes = "爬虫名", required = true)
    private String name;

    @ApiModelProperty(notes = "列表页正则表达式", required = true)
    private String listRegex;

    @ApiModelProperty(notes = "入口页", required = true)
    private String entryUrl;

    @ApiModelProperty(notes = "存储的表名", required = true)
    private String tableName;

    @ApiModelProperty(notes = "入口页字段规则json字符串")
    private String fieldsJson;

    @ApiModelProperty(notes = "正文页xpath")
    private String contentXpath;

    @ApiModelProperty(notes = "正文页规则json字符串")
    private String contentFieldsJson;

    @ApiModelProperty(notes = "页面爬取是否是用动态抓取 默认0启用 1.启用")
    private Integer isDynamic;
}
