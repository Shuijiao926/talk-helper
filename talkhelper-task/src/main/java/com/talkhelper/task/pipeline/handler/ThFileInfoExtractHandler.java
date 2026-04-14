package com.talkhelper.task.pipeline.handler;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件信息提取Handler
 * 从MultipartFile中提取文件名、大小等信息
 */
@Slf4j
@Component
public class ThFileInfoExtractHandler implements ThTaskCreateHandler {

    @Override
    public void handle(ThTaskCreateContext context) {
        MultipartFile file = context.getFile();
        
        if (file == null || file.isEmpty()) {
            log.warn("文件为空,使用默认值");
            context.setInputFileName(ThConstants.UNKNOWN);
            context.setInputFileSize(0L);
            return;
        }

        String fileName = file.getOriginalFilename();
        Long fileSize = file.getSize();

        context.setInputFileName(fileName != null ? fileName : ThConstants.UNKNOWN);
        context.setInputFileSize(fileSize);

        log.debug("文件信息提取完成: fileName={}, size={}", fileName, fileSize);
    }
}
