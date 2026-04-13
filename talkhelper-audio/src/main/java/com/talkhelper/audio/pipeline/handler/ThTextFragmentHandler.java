package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 步骤2：长文本分段切割处理器
 * 将段落按句子/标点切割成100-300字的短片段，防止TTS失败
 */
@Slf4j
@Component
public class ThTextFragmentHandler implements ThAudioProcessHandler {

    private static final int MAX_FRAGMENT_LENGTH = 300; // 最大片段长度
    private static final int MIN_FRAGMENT_LENGTH = 100; // 最小片段长度

    @Override
    public String getName() {
        return "文本分段切割处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getSegments() != null && context.getFragments() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始文本分段切割", getName());
        
        List<ThAudioProcessContext.TextFragment> fragments = new ArrayList<>();
        int fragmentIndex = 0;
        
        for (ThAudioProcessContext.PodcastSegment segment : context.getSegments()) {
            List<ThAudioProcessContext.TextFragment> segmentFragments = splitSegment(segment, fragmentIndex);
            fragments.addAll(segmentFragments);
            fragmentIndex += segmentFragments.size();
        }
        
        context.setFragments(fragments);
        
        log.info("[{}] 文本分段完成, 共 {} 个片段", getName(), fragments.size());
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 28, "文本分段完成");
        }
    }

    /**
     * 将段落分割为多个片段
     */
    private List<ThAudioProcessContext.TextFragment> splitSegment(
            ThAudioProcessContext.PodcastSegment segment, int startIndex) {
        
        List<ThAudioProcessContext.TextFragment> fragments = new ArrayList<>();
        String text = segment.getText();
        
        // 如果文本长度不超过限制，直接作为一个片段
        if (text.length() <= MAX_FRAGMENT_LENGTH) {
            ThAudioProcessContext.TextFragment fragment = ThAudioProcessContext.TextFragment.builder()
                    .index(startIndex)
                    .segmentIndex(segment.getIndex())
                    .role(segment.getRole())
                    .emotion(segment.getEmotion())
                    .text(text)
                    .length(text.length())
                    .pauseDuration(segment.getPauseDuration())
                    .build();
            fragments.add(fragment);
        } else {
            // 按句子分割
            List<String> sentences = splitBySentences(text);
            StringBuilder currentFragment = new StringBuilder();
            int currentIndex = startIndex;
            
            for (String sentence : sentences) {
                // 如果当前片段 + 新句子超过最大长度，先保存当前片段
                if (currentFragment.length() + sentence.length() > MAX_FRAGMENT_LENGTH 
                        && currentFragment.length() >= MIN_FRAGMENT_LENGTH) {
                    
                    ThAudioProcessContext.TextFragment fragment = ThAudioProcessContext.TextFragment.builder()
                            .index(currentIndex++)
                            .segmentIndex(segment.getIndex())
                            .role(segment.getRole())
                            .emotion(segment.getEmotion())
                            .text(currentFragment.toString().trim())
                            .length(currentFragment.length())
                            .pauseDuration(0.0) // 中间片段无停顿
                            .build();
                    fragments.add(fragment);
                    currentFragment.setLength(0);
                }
                
                currentFragment.append(sentence);
            }
            
            // 添加最后一个片段
            if (currentFragment.length() > 0) {
                ThAudioProcessContext.TextFragment fragment = ThAudioProcessContext.TextFragment.builder()
                        .index(currentIndex)
                        .segmentIndex(segment.getIndex())
                        .role(segment.getRole())
                        .emotion(segment.getEmotion())
                        .text(currentFragment.toString().trim())
                        .length(currentFragment.length())
                        .pauseDuration(segment.getPauseDuration()) // 最后一片段保留停顿
                        .build();
                fragments.add(fragment);
            }
        }
        
        return fragments;
    }

    /**
     * 按句子分割（保留标点符号）
     */
    private List<String> splitBySentences(String text) {
        List<String> sentences = new ArrayList<>();
        // 按句号、问号、感叹号、换行符分割
        String[] parts = text.split("(?<=[。！？\\n])");
        
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                sentences.add(part);
            }
        }
        
        return sentences;
    }
}
