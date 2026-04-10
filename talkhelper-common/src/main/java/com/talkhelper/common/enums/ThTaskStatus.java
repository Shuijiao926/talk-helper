package com.talkhelper.common.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum ThTaskStatus {

    /**
     * 待处理 - 任务已创建，等待Worker消费
     */
    PENDING("pending", "待处理"),

    /**
     * 处理中 - Worker正在执行
     */
    PROCESSING("processing", "处理中"),

    /**
     * 已完成 - 任务成功完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 失败 - 任务执行失败
     */
    FAILED("failed", "失败"),

    /**
     * 已取消 - 用户主动取消
     */
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String description;

    ThTaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ThTaskStatus fromCode(String code) {
        for (ThTaskStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("不支持的任务状态: " + code);
    }

    /**
     * 是否为终态
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
