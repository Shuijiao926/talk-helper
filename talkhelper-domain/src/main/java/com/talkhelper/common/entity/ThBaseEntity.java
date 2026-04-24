package com.talkhelper.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用Entity基类
 * 包含主键ID、创建时间、更新时间等通用字段
 */
@Data
public abstract class ThBaseEntity implements Serializable {

    /**
     * 主键ID(自增)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     * INSERT时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * INSERT和UPDATE时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
