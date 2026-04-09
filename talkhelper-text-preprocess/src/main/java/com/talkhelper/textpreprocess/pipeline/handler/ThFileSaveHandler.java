package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件保存处理器
 * 负责将上传的文件保存到临时目录
 */
@Slf4j
@Component
public class ThFileSaveHandler implements ThTextProcessHandler {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/talkhelper/uploads/";

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
    public void handle(ThTextProcessContext context) throws Exception {
        MultipartFile file = context.getFile();
        
        log.info("[{}] 开始保存文件: {}", getName(), file.getOriginalFilename());

        // 创建临时目录
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = Paths.get(TEMP_DIR + uniqueFilename);
        Files.write(filePath, file.getBytes());

        // 将文件路径存入上下文
        context.setFilePath(filePath.toString());
        
        log.info("[{}] 文件保存成功: {}", getName(), filePath);
    }
}
