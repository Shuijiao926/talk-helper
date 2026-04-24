package com.talkhelper.textpreprocess.strategy.cleaner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 口语化文本清洗策略
 * 将书面语转换为口语化表达,适合播客朗读
 */
@Slf4j
@Component
public class ThColloquialCleanerStrategy implements ThTextCleanerStrategy {

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

    @Override
    public String clean(String text) {
        log.info("开始口语化文本清洗, 原始长度: {}", text.length());

        String colloquialized = text;

        // 1. 替换书面连接词为口语化表达
        colloquialized = replaceFormalConnectors(colloquialized);

        // 2. 拆分长句为短句
        colloquialized = splitLongSentences(colloquialized);

        // 3. 统一数字、日期、单位的读法
        colloquialized = normalizeNumbersAndDates(colloquialized);

        // 4. 添加适当的停顿标记
        colloquialized = addPauseMarkers(colloquialized);

        log.info("口语化清洗完成, 处理后长度: {}", colloquialized.length());
        return colloquialized;
    }

    /**
     * 替换书面连接词
     */
    private String replaceFormalConnectors(String text) {
        return text
                // 替换正式连接词
                .replaceAll("综上所述", "总的来说")
                .replaceAll("由此可见", "可以看出")
                .replaceAll("鉴于此", "所以")
                .replaceAll("换言之", "也就是说")
                .replaceAll("此外", "另外")
                .replaceAll("然而", "但是")
                .replaceAll("因此", "所以")
                .replaceAll("故", "所以")
                .replaceAll("即", "就是")
                .replaceAll("亦", "也");
    }

    /**
     * 拆分长句
     */
    private String splitLongSentences(String text) {
        // 将超过80个字符的句子拆分为更短的句子
        String[] sentences = text.split("[。！？.!?]");
        StringBuilder result = new StringBuilder();

        for (String sentence : sentences) {
            if (sentence.length() > 80) {
                // 在逗号处拆分
                String[] parts = sentence.split(",|，");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        result.append(part.trim()).append("。");
                    }
                }
            } else {
                result.append(sentence).append("。");
            }
        }

        return result.toString();
    }

    /**
     * 标准化数字和日期
     */
    private String normalizeNumbersAndDates(String text) {
        // 标准化日期格式: 2026-04-06 -> 2026年4月6日
        Matcher matcher = DATE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            String replacement = year + "年" + month + "月" + day + "日";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 添加停顿标记(用于TTS)
     */
    private String addPauseMarkers(String text) {
        // 在适当位置添加停顿标记 <break time="500ms"/>
        // 这里简化处理,实际可以结合NLP分析
        return text.replaceAll("([。！？])", "$1 <break/>");
    }

    @Override
    public String getStrategyName() {
        return "colloquial";
    }
}
