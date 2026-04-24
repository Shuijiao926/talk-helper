package com.talkhelper.audio.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.talkhelper.audio.entity.ThAudioSegmentEntity;
import com.talkhelper.audio.mapper.ThAudioSegmentMapper;
import com.talkhelper.audio.service.ThAudioSegmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 音频片段 Service 实现
 */
@Slf4j
@Service
public class ThAudioSegmentServiceImpl
        extends ServiceImpl<ThAudioSegmentMapper, ThAudioSegmentEntity>
        implements ThAudioSegmentService {

    @Override
    public void batchSaveSegments(List<ThAudioSegmentEntity> segments) {
        if (segments == null || segments.isEmpty()) {
            return;
        }
        this.saveBatch(segments);
        log.info("批量保存音频片段记录: {} 条", segments.size());
    }

    @Override
    public List<ThAudioSegmentEntity> getSegmentsByTaskId(String taskId) {
        return lambdaQuery()
                .eq(ThAudioSegmentEntity::getTaskId, taskId)
                .orderByAsc(ThAudioSegmentEntity::getSegmentIndex)
                .list();
    }
}
