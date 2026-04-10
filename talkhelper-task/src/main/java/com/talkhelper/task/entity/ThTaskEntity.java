package com.talkhelper.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 异步任务实体
 */
@Data
@Builder
@TableName("th_task")
public class ThTaskEntity implements Serializable {

    /**
     * 任务ID（主键）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String taskId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 任务类型：text-preprocess（文本预处理）
     */
    private String taskType;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 进度百分比（0-100）
     */
    private Integer progress;

    /**
     * 当前阶段描述
     */
    private String currentStage;

    /**
     * 请求参数（JSON）
     */
    private String requestData;

    /**
     * 结果数据（JSON）
     */
    private String resultData;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 开始处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
