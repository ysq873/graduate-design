package com.lomoye.easy.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;
import java.util.List;

/**
 * 2020/8/19 09:40
 * yechangjun
 * 代理提供器
 */
@Slf4j
public abstract class AbstractDynamicProxyProvider implements ProxyProvider {

    /**
     * Return proxy to Provider when complete a download.
     *
     * @param proxy the proxy config contains host,port and identify info
     * @param page  the download result
     * @param task  the download task
     */
    @Override
    public void returnProxy(Proxy proxy, Page page, Task task) {
        //可以考录将用过的proxy保存起来，并标记好过期时间，在获取不到代理的时候进行服务降级，从用过的代理里轮训
    }

    /**
     * Get a proxy for task by some strategy.
     *
     * @param task the download task
     * @return proxy
     */
    @Override
    public Proxy getProxy(Task task) {
        try {
            List<Proxy> proxies = fetchProxy();
            if (CollectionUtils.isNotEmpty(proxies)) {
                Proxy proxy = proxies.get(0);
                log.info("getProxy proxy={}", proxy);
                return proxy;
            }
        } catch (Exception e) {
            log.error("getProxy error|id={}", task.getUUID(), e);
        }
        log.warn("no proxy can use|id={}", task.getUUID());
        return null;
    }

    //通过代理商获取代理
    abstract List<Proxy> fetchProxy();
}
