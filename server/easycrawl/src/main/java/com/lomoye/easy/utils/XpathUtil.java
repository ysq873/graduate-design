package com.lomoye.easy.utils;


import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.selector.XpathSelector;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yechangjun on 2022/3/22.
 */
public class XpathUtil {

    public static List<String> guessXpaths(String content, Document document) {
        Elements elements = document.getElementsContainingOwnText(content);

        List<String> rets = new ArrayList<>();
        for (Element e : elements) {
            rets.add(xpathSelector(e, true) + "/text()");
        }
        return rets;
    }

    public static String xpathSelector(Element e, boolean check) {
        String tagName = e.tagName();
        String selector = "";
        String classes = StringUtil.join(e.classNames(), " ");
        if (classes.length() > 0) {
            selector = "[@class=\"" + classes + "\"]";
        }

        if (e.parent() == null || e.parent() instanceof Document)  {
            return "/" + tagName;
        }
        if (check) {
            XpathSelector xpathSelector = new XpathSelector("/" + tagName + selector);
            Element parent = e.parent();

            if (xpathSelector.selectElements(parent).size() > 1) {
                selector = "/" + tagName + "[" + (e.elementSiblingIndex() + 1) + "]";
            } else {
                selector = "/" + tagName + selector;
            }
        } else {
            selector = "/" + tagName + selector;
        }


        return xpathSelector(e.parent(), false) + selector;
    }

//    public static String xpathSelector(Element e) {
//        if (e.id().length() > 0)
//            return "//*[@id=\"" + e.id() + "\"]";
//
//
//    }
}
