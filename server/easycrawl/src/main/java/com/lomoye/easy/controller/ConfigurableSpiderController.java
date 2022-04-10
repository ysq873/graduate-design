package com.lomoye.easy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.lomoye.easy.domain.ConfigurableSpider;
import com.lomoye.easy.exception.BusinessException;
import com.lomoye.easy.exception.ErrorCode;
import com.lomoye.easy.model.ListPatternModel;
import com.lomoye.easy.model.SpiderContentTestModel;
import com.lomoye.easy.model.SpiderSettingModel;
import com.lomoye.easy.model.SpiderTestModel;
import com.lomoye.easy.model.common.ResultData;
import com.lomoye.easy.model.common.ResultList;
import com.lomoye.easy.model.common.ResultPagedList;
import com.lomoye.easy.model.search.ConfigurableSpiderSearchModel;
import com.lomoye.easy.service.ConfigurableSpiderService;
import com.lomoye.easy.utils.HttpUtil;
import com.lomoye.easy.utils.LinkUtil;
import com.lomoye.easy.utils.XpathUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import us.codecraft.webmagic.selector.Html;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 2019/8/28 15:25
 * yechangjun
 * {
 * "entryUrl": "http://www.dianshangleida.com/list/appeal/1",
 * "listRegex": "http://www\\.dianshangleida\\.com/list/appeal/\\d+",
 * "fields": {"nick":"//*[@id='tam_newlist']/li/a/p[2]/span/text()",
 * "search_num":"//*[@id='tam_newlist']/li/a/p[3]/span/text()" },
 * "tableName": "ww_black_member"
 * }
 * <p>
 * {
 * "entryUrl": "http://www.xntk.net/mlist.php?t_id=0&page=0",
 * "listRegex": "http://www\\.xntk\\.net/mlist\\.php\\?t_id=0&page=\\d+",
 * "fields": {"book_name":"//*[@id='list']/tbody/tr/td[2]/a/text()",
 * "last_chapter":"//*[@id='list']/tbody/tr/td[3]/a/text()",
 * "type":"//*[@id='list']/tbody/tr/td[4]/font/text()",
 * "author":"//*[@id='list']/tbody/tr/td[5]/a/text()",
 * "update_time":"//*[@id='list']/tbody/tr/td[6]/small/text()" },
 * "tableName": "book"
 * }
 */
@RestController
@RequestMapping("/configurable-spider")
@Api(tags = "可配置化爬虫", description = "可配置化爬虫 lomoye")
public class ConfigurableSpiderController {

    @Autowired
    private ConfigurableSpiderService configurableSpiderService;

    @PostMapping
    @ApiOperation("增加一个可配置化爬虫")
    @ResponseBody
    public ResultData<ConfigurableSpider> addConfigurableSpider(@RequestBody ListPatternModel model) {
        if (model == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "提交参数不能为空");
        }
        if (Strings.isNullOrEmpty(model.getEntryUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "入口页不能为空");
        }
        if (Strings.isNullOrEmpty(model.getListRegex())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "列表页正则表达式不能为空");
        }
        if (Strings.isNullOrEmpty(model.getTableName())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "保存用的表名不能为空");
        }

        if (Strings.isNullOrEmpty(model.getFieldsJson()) || "[]".equals(model.getFieldsJson())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "入口页字段规则至少有一个");
        }

        if (!Strings.isNullOrEmpty(model.getContentXpath())) {
            if (Strings.isNullOrEmpty(model.getContentFieldsJson()) || "[]".equals(model.getContentFieldsJson())) {
                throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "正文页字段规则至少有一个");
            }
        }

        //查看表名有没有被占用
        QueryWrapper<ConfigurableSpider> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("table_name", model.getTableName());
        if (configurableSpiderService.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "表名已被使用: " + model.getTableName());
        }

        ConfigurableSpider spider = ConfigurableSpider.valueOf(model);
        configurableSpiderService.save(spider);

        return new ResultData<>(spider);
    }

    @PutMapping("/settings")
    @ApiOperation("配置爬虫运行参数")
    @ResponseBody
    public ResultData<ConfigurableSpider> configConfigurableSpiderRunParam(@RequestBody SpiderSettingModel model) {
        if (model == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "提交参数不能为空");
        }
        if (model.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "id不能为空");
        }

        ConfigurableSpider spider = configurableSpiderService.getById(model.getId());
        if (spider == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "爬虫不存在");
        }
        if (model.getThreadNum() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "线程数不能为空");
        }
        if (model.getSleepTime() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "页面处理完后的睡眠时间不能为空");
        }
        if (model.getRetryTimes() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "页面下载失败重试次数不能为空");
        }
        if (model.getRetrySleepTime() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "重试睡眠时间不能为空");
        }
        if (model.getCycleRetryTimes() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "页面爬取失败后放回队列的次数不能为空");
        }
        if (model.getTimeOut() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "下载页面超时时间不能为空");
        }

        spider.setThreadNum(model.getThreadNum());
        spider.setSleepTime(model.getSleepTime());
        spider.setRetryTimes(model.getRetryTimes());
        spider.setRetrySleepTime(model.getRetrySleepTime());
        spider.setCycleRetryTimes(model.getCycleRetryTimes());
        spider.setTimeOut(model.getTimeOut());
        spider.setModifyTime(LocalDateTime.now());
        configurableSpiderService.updateById(spider);

        return new ResultData<>(spider);
    }

    @PutMapping
    @ApiOperation("更新一个可配置化蜘蛛")
    @ResponseBody
    public ResultData<ConfigurableSpider> updateConfigurableSpider(@RequestBody ConfigurableSpider model) {
        if (model == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "提交参数不能为空");
        }
        if (Strings.isNullOrEmpty(model.getEntryUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "入口页不能为空");
        }
        if (Strings.isNullOrEmpty(model.getListRegex())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "列表页正则表达式不能为空");
        }
        if (Strings.isNullOrEmpty(model.getTableName())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "保存用的表名不能为空");
        }
        if (Strings.isNullOrEmpty(model.getFieldsJson()) || "[]".equals(model.getFieldsJson())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "入口页字段规则至少有一个");
        }

        if (!Strings.isNullOrEmpty(model.getContentXpath())) {
            if (Strings.isNullOrEmpty(model.getContentFieldsJson()) || "[]".equals(model.getContentFieldsJson())) {
                throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "正文页字段规则至少有一个");
            }
        }
        if (model.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "蜘蛛id不能为空");
        }
        ConfigurableSpider spider = configurableSpiderService.getById(model.getId());
        if (spider == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "蜘蛛不存在");
        }
        if (!model.getTableName().equals(spider.getTableName())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "表名不支持修改");
        }

        model.setModifyTime(LocalDateTime.now());
        configurableSpiderService.updateById(model);
        return new ResultData<>(spider);
    }

    @PostMapping("/config-proxy")
    @ApiOperation("配置代理")
    @ResponseBody
    public ResultData<ConfigurableSpider> configSpiderProxy(@RequestBody ConfigurableSpider model) {
        ConfigurableSpider spider = configurableSpiderService.getById(model.getId());
        if (spider == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "蜘蛛不存在");
        }
        spider.setProxyChannelId(Strings.isNullOrEmpty(model.getProxyChannelId()) ? "" : model.getProxyChannelId());
        spider.setModifyTime(LocalDateTime.now());
        configurableSpiderService.updateById(spider);
        return new ResultData<>(spider);
    }

    @PostMapping("/search")
    @ApiOperation("分页搜索")
    @ResponseBody
    public ResultPagedList<ConfigurableSpider> searchConfigurableSpider(@RequestBody ConfigurableSpiderSearchModel searchModel) {
        IPage<ConfigurableSpider> page = new Page<>(searchModel.getPageNo(), searchModel.getPageSize());
        QueryWrapper<ConfigurableSpider> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(!Strings.isNullOrEmpty(searchModel.getName()), ConfigurableSpider::getName, searchModel.getName())
                .like(!Strings.isNullOrEmpty(searchModel.getTableName()), ConfigurableSpider::getTableName, searchModel.getTableName())
                .orderByDesc(ConfigurableSpider::getCreateTime);


        page = configurableSpiderService.page(page, queryWrapper);
        return new ResultPagedList<>(page.getRecords(), page.getTotal(), searchModel);
    }

    @PostMapping("/export/{id}")
    @ApiOperation("导出配置")
    @ResponseBody
    public ResultData<ConfigurableSpider> export(@PathVariable String id) {
        //防御作用,id不能为空
        Preconditions.checkArgument(id != null);
        ConfigurableSpider configurableSpider = configurableSpiderService.getById(id);
        if (configurableSpider != null) {
            configurableSpider.setCreateTime(null);
            configurableSpider.setId(null);
            configurableSpider.setModifyTime(null);
            return new ResultData<>(configurableSpider);
        } else {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "当前没有可导出的配置信息");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除爬虫")
    @ResponseBody
    public ResultData<Boolean> deleteConfigurableSpider(@PathVariable String id) {
        Preconditions.checkArgument(id != null);
        configurableSpiderService.removeById(id);
        return new ResultData<>(true);
    }

    @PostMapping("/test-xpath")
    @ApiOperation("测试xpath")
    @ResponseBody
    public ResultList<String> testXpath(@RequestBody SpiderTestModel testModel) {
        if (Strings.isNullOrEmpty(testModel.getUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入网址");
        }
        if (Strings.isNullOrEmpty(testModel.getXpath())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入XPATH");
        }
        Html html = downloadPage(testModel.getUrl(), testModel.getIsDynamic());

        return new ResultList<>(html.xpath(testModel.getXpath()).all());
    }

    @PostMapping("/test-content-xpath")
    @ApiOperation("测试正文页xpath")
    @ResponseBody
    public ResultList<String> testContentXpath(@RequestBody SpiderContentTestModel testModel) {
        if (Strings.isNullOrEmpty(testModel.getEntryUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入口页网址");
        }
        if (Strings.isNullOrEmpty(testModel.getContentXpath())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入正文页XPATH");
        }
        if (Strings.isNullOrEmpty(testModel.getXpath())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入字段XPATH");
        }
        Html html = downloadPage(testModel.getEntryUrl(), testModel.getIsDynamic());

        List<String> links = html.xpath(testModel.getContentXpath()).all();
        if (CollectionUtils.isEmpty(links)) {
            return new ResultList<>();
        }
        String testLink = links.get(0);
        testLink = LinkUtil.getAbsoluteURL(testModel.getEntryUrl(), testLink);

        Html testPage = downloadPage(testLink, testModel.getIsDynamic());
        List<String> values = testPage.xpath(testModel.getXpath()).all();
        values.add(0, testLink);
        return new ResultList<>(values);
    }

    @PostMapping("/test-regex")
    @ApiOperation("测试正则")
    @ResponseBody
    public ResultList<String> testRegex(@RequestBody SpiderTestModel testModel) {
        if (Strings.isNullOrEmpty(testModel.getUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入网址");
        }
        if (Strings.isNullOrEmpty(testModel.getRegex())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入正则");
        }
        Html html = downloadPage(testModel.getUrl(), testModel.getIsDynamic());
        return new ResultList<>(html.links().regex(testModel.getRegex()).all());
    }

    @PostMapping("/test-page")
    @ApiOperation("测试页面")
    @ResponseBody
    public ResultData<String> testPage(@RequestBody SpiderTestModel testModel) {
        if (Strings.isNullOrEmpty(testModel.getUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入网址");
        }
        Html html = downloadPage(testModel.getUrl(), testModel.getIsDynamic());
        return new ResultData<>(html.toString());
    }

    @PostMapping("/guess-xpath")
    @ApiOperation("根据文字联想xpath规则")
    @ResponseBody
    public ResultList<String> guessXpath(@RequestBody SpiderTestModel testModel) {
        if (Strings.isNullOrEmpty(testModel.getUrl())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入网址");
        }
        if (Strings.isNullOrEmpty(testModel.getContent())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "请输入联想文字");
        }
        Html html = downloadPage(testModel.getUrl(), testModel.getIsDynamic());
        return new ResultList<>(XpathUtil.guessXpaths(testModel.getContent(), html.getDocument()));
    }


    private Html downloadPage(String url, Integer isDynamic) {
        Html html;
        if (isDynamic != null && isDynamic == 1) {
            html = HttpUtil.downloadDynamic(url);
        } else {
            html = HttpUtil.download(url);
        }
        if (html == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "网页下载失败");
        }
        return html;
    }


}
