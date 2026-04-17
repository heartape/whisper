package com.heartape.whisper.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class XssCleaner {

    /**
     * 清洗文本，去除所有 HTML 标签 (适用于纯文本聊天)
     */
    public String clean(String content) {
        if (content == null) return null;
        // Safelist.none() 会移除所有 HTML 标签，只保留文本
        return Jsoup.clean(content, Safelist.none());
    }

    /**
     * 如果聊天支持富文本 (如加粗、链接)，使用此方法
     */
    public String cleanRichText(String content) {
        if (content == null) return null;
        // 允许基础格式：b, em, i, strong, u, a, li, ul, ol...
        // 自动移除 script, iframe, object, onclick 等危险内容
        return Jsoup.clean(content, Safelist.basic());
    }

}
