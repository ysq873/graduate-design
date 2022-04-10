package com.lomoye.easy.utils;

import org.assertj.core.util.Strings;

import java.net.URI;
import java.net.URL;

/**
 * 2020/11/9 13:45
 * yechangjun
 * 链接合法转化工具
 */
public class LinkUtil {

    /**
     * 获取域名
     *
     * @param url 链接
     * @return 域名
     */
    public static String getDomain(String url) {
        if (Strings.isNullOrEmpty(url)) {
            return url;
        }
        String domain;
        if (url.contains("//")) {
            domain = url.substring(url.indexOf("//") + 2);
        } else {
            domain = url;
        }

        if (!domain.contains("/")) {
            return domain;
        }

        return domain.substring(0, domain.indexOf("/"));
    }

    public static String getAbsoluteURL(String baseURI, String relativePath) {
        String abURL = null;
        try {
            URI base = new URI(baseURI);
            URI abs = base.resolve(relativePath);//解析于上述网页的相对URL，得到绝对URI
            URL absURL = abs.toURL();//转成URL
            abURL = absURL.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abURL;
    }
}