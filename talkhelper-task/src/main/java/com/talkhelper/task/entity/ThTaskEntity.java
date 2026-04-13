package com.talkhelper.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.talkhelper.common.entity.ThBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异步任务实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("th_task")
public class ThTaskEntity extends ThBaseEntity {

    /**
     * 任务ID(UUID)
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 任务类型：text-preprocess（文本预处理）
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 任务状态
     */
    @TableField("status")
    private String status;

    /**
     * 进度百分比（0-100）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 当前阶段描述
     */
    @TableField("current_stage")
    private String currentStage;

    /**
     * 请求参数（JSON）
     */
    @TableField("request_data")
    private String requestData;

    /**
     * 结果数据（JSON）
     */
    @TableField("result_data")
    private String resultData;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    // ==================== 输入文件信息 ====================

    /**
     * 输入文件名
     */
    @TableField("input_file_name")
    private String inputFileName;

    /**
     * 输入文件MinIO URL
     */
    @TableField("input_file_url")
    private String inputFileUrl;

    /**
     * 输入文件大小(字节)
     */
    @TableField("input_file_size")
    private Long inputFileSize;

    // ==================== 输出文件信息 ====================

    /**
     * 输出文件名
     */
    @TableField("output_file_name")
    private String outputFileName;

    /**
     * 输出文件MinIO URL
     */
    @TableField("output_file_url")
    private String outputFileUrl;

    /**
     * 输出文件大小(字节)
     */
    @TableField("output_file_size")
    private Long outputFileSize;

    /**
     * 开始处理时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
