package com.talkhelper.common.enums;

import lombok.Getter;

/**
 * 文本分块策略枚举
 */
@Getter
public enum ThTextChunkStrategy {

    /**
     * 按章节分块
     */
    CHAPTER("chapter", "按章节"),

    /**
     * 按段落分块
     */
    PARAGRAPH("paragraph", "按段落"),

    /**
     * 固定长度分块
     */
    FIXED("fixed", "固定长度"),

    /**
     * 语义分块
     */
    SEMANTIC("semantic", "语义分块");

    private final String code;
    private final String description;

    ThTextChunkStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 策略代码
     * @return 对应的枚举值
     */
    public static ThTextChunkStrategy fromCode(String code) {
        if (code == null) {
            return FIXED; // 默认使用固定长度分块
        }
        for (ThTextChunkStrategy strategy : values()) {
            if (strategy.getCode().equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("不支持的分块策略: " + code);
    }

    /**
     * 判断是否支持该分块策略
     *
     * @param code 策略代码
     * @return 是否支持
     */
    public static boolean isSupported(String code) {
        if (code == null) {
            return false;
        }
        for (ThTextChunkStrategy strategy : values()) {
            if (strategy.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
