package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.enums.ThContentSaveStrategy;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.strategy.saver.ThContentSaverFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThMultiStrategySaverExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件保存处理器
 * 负责将上传的文件保存到临时目录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThFileSaveHandler implements ThTextProcessHandler {

    private final ThContentSaverFactory saverFactory;
    private final ThMultiStrategySaverExecutor multiStrategyExecutor;

    @Override
    public String getName() {
        return "文件保存处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        // 只有当有文件输入时才执行
        return context.getFile() != null;
    }

    @Override
    public void handle(ThTextProcessContext context) {
        MultipartFile file = context.getFile();
        
        log.info("[{}] 开始保存文件: {}", getName(), file.getOriginalFilename());

        // 获取保存策略列表（默认使用本地文件保存）
        ThContentSaveStrategy[] strategies = context.getRequest() != null && context.getRequest().getSaveStrategies() != null
                ? context.getRequest().getSaveStrategies()
                : new ThContentSaveStrategy[]{ThContentSaveStrategy.LOCAL_FILE};

        // 使用多策略执行器保存文件
        multiStrategyExecutor.execute(
                strategies,
                saver -> {
                    String savedPath = saver.save(null, file);
                    // 将第一个保存路径存入上下文
                    if (context.getFilePath() == null) {
                        context.setFilePath(savedPath);
                    }
                },
                ThConstants.ACTION_FILE_SAVE
        );
        
        log.info("[{}] 文件保存完成，主路径: {}", getName(), context.getFilePath());
    }
}
