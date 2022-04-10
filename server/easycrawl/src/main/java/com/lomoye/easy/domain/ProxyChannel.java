package com.lomoye.easy.domain;

import lombok.Data;

/**
 * 2020/8/19 14:13
 * yechangjun
 * 代理渠道
 */
@Data
public class ProxyChannel extends CommonDomain {

    //代理商名字
    private String name;

    //别名
    private String alias;

    //代理商渠道代号
    private String code;

    //提取码
    private String token;




}
