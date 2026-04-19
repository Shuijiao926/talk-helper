package com.talkhelper.audio.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talkhelper.common.entity.ThBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 音频片段实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("th_audio_segment")
public class ThAudioSegmentEntity extends ThBaseEntity {

    @TableField("segment_id")
    private String segmentId;

    @TableField("task_id")
    private String taskId;

    @TableField("segment_index")
    private Integer segmentIndex;

    @TableField("role")
    private String role;

    @TableField("audio_url")
    private String audioUrl;

    @TableField("file_size")
    private Long fileSize;

    @TableField("format")
    private String format;

    @TableField("duration")
    private Double duration;
}
