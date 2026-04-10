package com.talkhelper.common.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 */
@Getter
public enum ThFileType {
    
    TXT("txt", "文本文件"),
    MD("md", "Markdown文件"),
    HTML("html", "HTML文件"),
    HTM("htm", "HTML文件"),
    PDF("pdf", "PDF文档"),
    DOCX("docx", "Word文档"),
    EPUB("epub", "EPUB电子书");

    private final String code;
    private final String description;

    ThFileType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 文件类型代码
     * @return 对应的枚举值
     */
    public static ThFileType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ThFileType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的文件类型: " + code);
    }

    /**
     * 判断是否支持该文件类型
     *
     * @param code 文件类型代码
     * @return 是否支持
     */
    public static boolean isSupported(String code) {
        if (code == null) {
            return false;
        }
        for (ThFileType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
