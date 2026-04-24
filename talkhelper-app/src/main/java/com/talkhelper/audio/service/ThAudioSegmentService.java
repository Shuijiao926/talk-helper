package com.talkhelper.audio.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.talkhelper.audio.entity.ThAudioSegmentEntity;

import java.util.List;

/**
 * 音频片段 Service
 */
public interface ThAudioSegmentService extends IService<ThAudioSegmentEntity> {

    /**
     * 批量保存音频片段记录
     */
    void batchSaveSegments(List<ThAudioSegmentEntity> segments);

    /**
     * 按任务ID查询所有片段（按序号排序）
     */
    List<ThAudioSegmentEntity> getSegmentsByTaskId(String taskId);
}
