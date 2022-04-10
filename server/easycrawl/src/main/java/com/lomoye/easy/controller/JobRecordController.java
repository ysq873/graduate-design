package com.lomoye.easy.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.lomoye.easy.domain.Job;
import com.lomoye.easy.exception.BusinessException;
import com.lomoye.easy.exception.ErrorCode;
import com.lomoye.easy.model.common.ResultData;
import com.lomoye.easy.model.common.ResultPagedList;
import com.lomoye.easy.model.search.JobRecordSearchModel;
import com.lomoye.easy.service.JobService;
import com.lomoye.easy.utils.ExcelExportUtil;
import com.lomoye.easy.utils.ExcelExporter;
import com.lomoye.easy.utils.LocalDateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 2019/9/1 19:22
 * yechangjun
 */
@Slf4j
@RestController
@RequestMapping("/job-record")
@Api(tags = "爬虫任务结果", description = "爬虫任务结果 lomoye")
public class JobRecordController {

    @Autowired
    private JobService jobService;

    @PostMapping("/search")
    @ApiOperation("分页查询")
    @ResponseBody
    public ResultPagedList<Map<String, Object>> searchJobRecord(@RequestBody JobRecordSearchModel searchModel) {
        if (Strings.isNullOrEmpty(searchModel.getJobUuid())) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "jobUuid不能为空");
        }

        Job job = jobService.getById(searchModel.getJobUuid());
        if (job == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "任务不存在");
        }

        try {
            String select = "SELECT *";
            String sqlExceptSelect = " FROM " + job.getSpiderTableName() + " WHERE job_uuid = ? ORDER BY id DESC" ;
            Page<Record> page = Db.paginate(searchModel.getPageNo().intValue(), searchModel.getPageSize().intValue(), select, sqlExceptSelect, searchModel.getJobUuid());
            List<Record> records = page.getList();
            if (records == null) {
                records = new ArrayList<>();
            }

            List<Map<String, Object>> rds = records.stream().map(Record::getColumns).collect(Collectors.toList());
            //转换一下TimeStamp成string
            for (Map<String, Object> row : rds) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    if (entry.getValue() instanceof Timestamp) {
                        entry.setValue(LocalDateUtil.getDateTimeAsString(LocalDateUtil.getDateTimeOfTimestamp(((Timestamp) entry.getValue()).getTime())));
                    }
                }
            }
            return new ResultPagedList<>(rds, (long) page.getTotalRow(), searchModel);
        } catch (Exception e) {
            //动态表可能还没创建
            log.info("searchJobRecord error", e);

            return new ResultPagedList<>(new ArrayList<>(), 0L, searchModel);
        }
    }


    @GetMapping("/export-excel")
    @ApiOperation("导出数据到excel")
    @ResponseBody
    public ResultData<Boolean> export(@ApiIgnore HttpServletRequest request, @ApiIgnore HttpServletResponse response, String jobUuid) {
        if (Strings.isNullOrEmpty(jobUuid)) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "jobUuid不能为空");
        }

        Job job = jobService.getById(jobUuid);
        if (job == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ILLEGAL, "任务不存在");
        }

        try {
            String sql = "SELECT * FROM " + job.getSpiderTableName() + " WHERE job_uuid = ? ORDER BY id" ;
            List<Record> records = Db.find(sql, jobUuid);
            if (records == null) {
                records = new ArrayList<>();
            }

            List<Map<String, Object>> rds = records.stream().map(Record::getColumns).collect(Collectors.toList());
            //转换一下TimeStamp成string
            for (Map<String, Object> row : rds) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    if (entry.getValue() instanceof Timestamp) {
                        entry.setValue(LocalDateUtil.getDateTimeAsString(LocalDateUtil.getDateTimeOfTimestamp(((Timestamp) entry.getValue()).getTime())));
                    }
                }
            }

            Workbook workbook = ExcelExporter.exportExcelFromMapList(job.getSpiderName(), rds, Lists.newArrayList(rds.get(0).keySet()), Lists.newArrayList(rds.get(0).keySet()));
            ExcelExportUtil.export(request, response, workbook, job.getSpiderName() + ".xlsx");
            return new ResultData<>(true);
        } catch (Exception e) {
            //动态表可能还没创建
            log.info("export JobRecord error", e);

            return new ResultData<>(false);
        }
    }
}