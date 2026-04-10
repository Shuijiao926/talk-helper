package com.talkhelper.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talkhelper.task.entity.ThTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Mapper
 */
@Mapper
public interface ThTaskMapper extends BaseMapper<ThTaskEntity> {
}
