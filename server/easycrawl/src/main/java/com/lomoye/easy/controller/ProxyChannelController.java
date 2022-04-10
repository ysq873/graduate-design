package com.lomoye.easy.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Preconditions;
import com.lomoye.easy.constants.ProxyChannelCode;
import com.lomoye.easy.domain.ProxyChannel;
import com.lomoye.easy.exception.BusinessException;
import com.lomoye.easy.exception.ErrorCode;
import com.lomoye.easy.model.common.ResultData;
import com.lomoye.easy.model.common.ResultList;
import com.lomoye.easy.model.search.ProxyChannelSearchModel;
import com.lomoye.easy.service.ProxyChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 2019/9/1 19:22
 * yechangjun
 */
@Slf4j
@RestController
@RequestMapping("/proxy-channel")
@Api(tags = "爬虫代理", description = "爬虫代理 lomoye")
public class ProxyChannelController {

    @Autowired
    private ProxyChannelService proxyChannelService;


    @PostMapping
    @ApiOperation("创建代理")
    @ResponseBody
    public ResultData<ProxyChannel> addProxy(@RequestBody ProxyChannel proxyChannel) {
        Preconditions.checkArgument(proxyChannel != null);

        if (ProxyChannelCode.CODE_MAP.get(proxyChannel.getCode()) == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "代理厂商代号错误");
        }

        if (Strings.isBlank(proxyChannel.getToken())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入IP提取码");
        }

        if (Strings.isBlank(proxyChannel.getAlias())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入代理别名");
        }

        proxyChannel.setName(ProxyChannelCode.CODE_MAP.get(proxyChannel.getCode()));

        proxyChannel.setCreateTime(LocalDateTime.now());
        proxyChannel.setModifyTime(LocalDateTime.now());
        proxyChannelService.save(proxyChannel);

        return new ResultData<>(proxyChannel);
    }


    @PostMapping("/list")
    @ApiOperation("代理列表")
    @ResponseBody
    public ResultList<ProxyChannel> listProxyChannel(@RequestBody ProxyChannelSearchModel searchModel) {
        QueryWrapper<ProxyChannel> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .orderByDesc(ProxyChannel::getCreateTime);


        List<ProxyChannel> proxyChannelList = proxyChannelService.list(queryWrapper);

        return new ResultList<>(proxyChannelList != null ? proxyChannelList : new ArrayList<>());
    }
}