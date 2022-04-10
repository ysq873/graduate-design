package com.lomoye.easy.proxy;

import com.google.common.base.Preconditions;
import com.lomoye.easy.constants.ProxyChannelCode;
import com.lomoye.easy.domain.ProxyChannel;
import org.assertj.core.util.Strings;
import us.codecraft.webmagic.proxy.ProxyProvider;

/**
 * 2020/8/20 13:45
 * yechangjun
 */
public class ProxyProviderFactory {
    public static ProxyProvider build(ProxyChannel proxyChannel) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(proxyChannel.getCode()));
        switch (proxyChannel.getCode()) {
            case ProxyChannelCode.DATA5U:
                return new Data5uProxyProvider(proxyChannel.getToken());

            default:
                throw new RuntimeException("invalid proxy channel code");
        }

    }
}
