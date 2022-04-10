package com.lomoye.easy.proxy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import us.codecraft.webmagic.proxy.Proxy;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * 2020/8/20 13:50
 * yechangjun
 */
@Slf4j
public class Data5uProxyProvider extends AbstractDynamicProxyProvider {

    //提取码
    private String code;

    public Data5uProxyProvider(String code) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(code));
        this.code = code;
    }


    @Override
    List<Proxy> fetchProxy() {
        String apiUrl = "http://api.ip.data5u.com/dynamic/get.html?order=" + code + "&random=1&sep=3";
        int time = 0;

        while(time < 3) {

            time++;
            try {
                Thread.sleep(100);
                java.net.URL url = new java.net.URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(3000);
                connection = (HttpURLConnection)url.openConnection();

                InputStream raw = connection.getInputStream();
                InputStream in = new BufferedInputStream(raw);
                byte[] data = new byte[in.available()];
                int bytesRead;
                int offset = 0;
                while(offset < data.length) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if(bytesRead == -1) {
                        break;
                    }
                    offset += bytesRead;
                }
                in.close();
                raw.close();
                String[] res = new String(data, "UTF-8").split("\n");

                List<Proxy> proxies = Lists.newArrayList();
                for (String ip : res) {
                    Proxy proxy = new Proxy(ip.split(":")[0], Integer.valueOf(ip.split(":")[1]));
                    proxies.add(proxy);
                }
                return proxies;
            } catch (Exception e) {
                log.warn("fetchProxy error, " + e.getMessage());
            }
        }

        return Lists.newArrayList();
    }
}
