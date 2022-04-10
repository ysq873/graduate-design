package com.lomoye.easy.component;

import us.codecraft.webmagic.downloader.Downloader;

import java.io.IOException;

public class DownloaderFactory {

    private static SeleniumDownloader seleniumDownloader = new SeleniumDownloader("/usr/local/bin/chromedriver");

    public static Downloader getDownloader(Integer isDynamic) {
        if (isDynamic != null && isDynamic == 1) {
            //太费资源，全局公用一个实例
            return seleniumDownloader;
        } else {
            return new DefaultHttpClientDownloader();
        }
    }

    public static void close() throws IOException {
        seleniumDownloader.close();
    }
}
