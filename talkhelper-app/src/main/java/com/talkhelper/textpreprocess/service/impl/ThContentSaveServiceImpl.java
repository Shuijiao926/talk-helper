package com.talkhelper.textpreprocess.service.impl;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.enums.ThContentSaveStrategy;
import com.talkhelper.textpreprocess.service.ThContentSaveService;
import com.talkhelper.textpreprocess.strategy.saver.ThContentSaverFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThMultiStrategySaverExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容保存服务实现
 * 提供解耦的、独立的内容保存能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThContentSaveServiceImpl implements ThContentSaveService {

    private final ThContentSaverFactory saverFactory;
    private final ThMultiStrategySaverExecutor multiStrategyExecutor;

    @Override
    public String saveFile(MultipartFile file, ThContentSaveStrategy... strategies) {
        if (file == null || file.isEmpty()) {
            log.warn("文件为空，跳过保存");
            return null;
        }

        // 使用默认策略或指定策略
        ThContentSaveStrategy[] saveStrategies = strategies.length > 0 
                ? strategies 
                : new ThContentSaveStrategy[]{ThContentSaveStrategy.LOCAL_FILE};

        log.info("开始保存文件: {}, 策略数: {}", file.getOriginalFilename(), saveStrategies.length);

        final String[] mainPath = {null};
        multiStrategyExecutor.execute(
                saveStrategies,
                saver -> {
                    String savedPath = saver.save(null, file);
                    if (mainPath[0] == null) {
                        mainPath[0] = savedPath;
                    }
                },
                ThConstants.ACTION_FILE_SAVE
        );

        log.info("文件保存完成，主路径: {}", mainPath[0]);
        return mainPath[0];
    }

    @Override
    public String saveText(String content, String fileName, ThContentSaveStrategy... strategies) {
        if (content == null || content.isEmpty()) {
            log.warn("文本内容为空，跳过保存");
            return null;
        }

        ThContentSaveStrategy[] saveStrategies = strategies.length > 0 
                ? strategies 
                : new ThContentSaveStrategy[]{ThContentSaveStrategy.LOCAL_FILE};

        String safeFileName = fileName != null ? fileName : "text_" + System.currentTimeMillis() + ".txt";
        log.info("开始保存文本: {}, 长度: {}, 策略数: {}", safeFileName, content.length(), saveStrategies.length);

        final String[] mainPath = {null};
        multiStrategyExecutor.execute(
                saveStrategies,
                saver -> {
                    String savedPath = saver.save(content, null);
                    if (mainPath[0] == null) {
                        mainPath[0] = savedPath;
                    }
                },
                ThConstants.ACTION_TEXT_SAVE
        );

        log.info("文本保存完成，主路径: {}", mainPath[0]);
        return mainPath[0];
    }

    @Override
    public List<String> saveChunks(List<String> chunks, String baseFileName, ThContentSaveStrategy... strategies) {
        if (chunks == null || chunks.isEmpty()) {
            log.warn("文本分块列表为空，跳过保存");
            return new ArrayList<>();
        }

        ThContentSaveStrategy[] saveStrategies = strategies.length > 0 
                ? strategies 
                : new ThContentSaveStrategy[]{ThContentSaveStrategy.LOCAL_FILE};

        log.info("开始保存 {} 个文本分块, 基础文件名: {}, 策略数: {}", 
                chunks.size(), baseFileName, saveStrategies.length);

        List<String> savedPaths = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String chunkFileName = baseFileName != null 
                    ? baseFileName.replace(".", "_chunk" + i + ".") 
                    : "chunk_" + i + ".txt";

            final String[] chunkPath = {null};
            multiStrategyExecutor.execute(
                    saveStrategies,
                    saver -> {
                        String savedPath = saver.save(chunk, null);
                        if (chunkPath[0] == null) {
                            chunkPath[0] = savedPath;
                        }
                    },
                    ThConstants.ACTION_CHUNK_TEXT_SAVE
            );

            if (chunkPath[0] != null) {
                savedPaths.add(chunkPath[0]);
            }
        }

        log.info("文本分块保存完成，共保存 {} 个文件", savedPaths.size());
        return savedPaths;
    }

    @Override
    public List<String> getSupportedStrategies() {
        return saverFactory.getSupportedStrategies();
    }
}
