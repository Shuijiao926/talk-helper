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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("th_task")
public class ThTaskEntity extends ThBaseEntity {

    @TableField("task_id")
    private String taskId;

    @TableField("user_id")
    private String userId;

    @TableField("task_type")
    private String taskType;

    @TableField("status")
    private String status;

    @TableField("progress")
    private Integer progress;

    @TableField("current_stage")
    private String currentStage;

    @TableField("request_data_url")
    private String requestDataUrl;

    @TableField("result_data_url")
    private String resultDataUrl;

    @TableField("error_message")
    private String errorMessage;

    @TableField("input_file_name")
    private String inputFileName;

    @TableField("input_file_url")
    private String inputFileUrl;

    @TableField("input_file_size")
    private Long inputFileSize;

    @TableField("output_file_name")
    private String outputFileName;

    @TableField("output_file_url")
    private String outputFileUrl;

    @TableField("output_file_size")
    private Long outputFileSize;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
