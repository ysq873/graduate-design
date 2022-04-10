package com.lomoye.easy.utils;

import com.lomoye.easy.component.DefaultHttpClientDownloader;
import com.lomoye.easy.component.DownloaderFactory;
import com.lomoye.easy.component.SeleniumDownloader;
import us.codecraft.webmagic.selector.Html;


/**
 * 2020/9/6 14:22
 * yechangjun
 */
public class HttpUtil {

    private final static DefaultHttpClientDownloader httpClientDownloader = new DefaultHttpClientDownloader();

    public static Html download(String url) {
        return httpClientDownloader.download(url);
    }

    public static Html downloadDynamic(String url) {
        return ((SeleniumDownloader) DownloaderFactory.getDownloader(1)).download(url);
    }

    public static void main(String[] args) {
        System.out.println(downloadDynamic("http://www.moa.gov.cn/gk/tzgg_1/bl/index.htm"));
    }
}
