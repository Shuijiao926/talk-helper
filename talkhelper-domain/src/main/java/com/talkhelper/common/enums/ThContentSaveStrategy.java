package com.talkhelper.common.enums;

/**
 * 内容保存策略枚举
 */
public enum ThContentSaveStrategy {

    /**
     * 本地文件保存
     */
    LOCAL_FILE("local-file", "本地文件"),

    /**
     * 关系型数据库保存
     */
    DATABASE("database", "关系型数据库"),

    /**
     * 向量数据库保存
     */
    VECTOR_DB("vector-db", "向量数据库");

    private final String code;
    private final String description;

    ThContentSaveStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 策略代码
     * @return 对应的枚举值
     */
    public static ThContentSaveStrategy fromCode(String code) {
        if (code == null) {
            return LOCAL_FILE; // 默认使用本地文件保存
        }
        for (ThContentSaveStrategy strategy : values()) {
            if (strategy.getCode().equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("不支持的保存策略: " + code);
    }

    /**
     * 判断是否支持该保存策略
     *
     * @param code 策略代码
     * @return 是否支持
     */
    public static boolean isSupported(String code) {
        if (code == null) {
            return false;
        }
        for (ThContentSaveStrategy strategy : values()) {
            if (strategy.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
