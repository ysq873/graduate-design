package com.lomoye.easy.utils;

import com.google.common.base.Strings;
import com.lomoye.easy.exception.BusinessException;
import com.lomoye.easy.exception.ErrorCode;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Created by lomoye on 2017/5/11.
 * ^_^ web端导出excel工具
 */
public class ExcelExportUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportUtil.class);

    public static void export(HttpServletRequest request, HttpServletResponse response, Workbook workbook, String fileName) {
        try {
            //火狐游览器中文文件名会有乱码,兼容火狐
            String agent = request.getHeader("USER-AGENT").toLowerCase();

            if (!Strings.isNullOrEmpty(agent) && agent.contains("firefox")) {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            } else {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            }

            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.warn("export excel failed|fileName={}", fileName, e);
            throw new BusinessException(ErrorCode.EXCEL_EXPORT_ERROR, "excel导出失败");
        }

    }
}
