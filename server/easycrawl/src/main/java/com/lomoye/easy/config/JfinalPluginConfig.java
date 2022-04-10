package com.lomoye.easy.config;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 2020/3/5 13:11
 * yechangjun
 * jfinal操作数据库的配置类
 */
@Configuration
public class JfinalPluginConfig implements InitializingBean {

    @Autowired
    private DataSourceProperties dataSourceProperties;


    @Override
    public void afterPropertiesSet() throws Exception {
        //jfinal的Db插件配置
        HikariCpPlugin hikariCpPlugin = new HikariCpPlugin(dataSourceProperties.getUrl(), dataSourceProperties.getUsername(), dataSourceProperties.getPassword());
        hikariCpPlugin.start();
        ActiveRecordPlugin arp = new ActiveRecordPlugin(hikariCpPlugin);
        //用于支持查询出来的字段次序与 select a, b, c... 的次序一致
        arp.setContainerFactory(new OrderedFieldContainerFactory());
        arp.start();
    }
}
