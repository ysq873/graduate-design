package com.lomoye.easy.backend;

import com.google.common.collect.Lists;
import com.lomoye.easy.domain.ConfigurableSpider;
import com.lomoye.easy.service.ConfigurableSpiderService;
import com.lomoye.easy.utils.SerializationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 2020/4/10 14:18
 * yechangjun
 * 导入示例爬虫配置
 */
@Slf4j
@Service
public class ImportExampleService extends Thread {

    @Autowired
    private ConfigurableSpiderService configurableSpiderService;

    private static final List<String> exampleFileNames = Lists.newArrayList("douban.txt", "hangzhouloupan.txt");

    @Override
    public void run() {
        //如果数据库里一个爬虫都没有启动的时候创建示例爬虫
        log.info("importExampleService start");
        if (configurableSpiderService.count() > 0) {
            log.info("importExampleService example already create");
            return;
        }

        for (String fileName : exampleFileNames) {
            InputStream in = getClass().getResourceAsStream("/example/" + fileName);
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                final StringBuilder config = new StringBuilder();
                reader.lines().forEach(config::append);
                doImport(config.toString());
            } catch (Exception e) {
                log.error("doImport spider error", e);
            }
        }
    }

    public static void main(String[] args) {
        new ImportExampleService().run();
    }

    private void doImport(String config) {
        ConfigurableSpider configurableSpider = SerializationUtil.str2Object(config, ConfigurableSpider.class);
        configurableSpider.setCreateTime(LocalDateTime.now());
        configurableSpider.setModifyTime(LocalDateTime.now());
        configurableSpiderService.save(configurableSpider);
    }
}
