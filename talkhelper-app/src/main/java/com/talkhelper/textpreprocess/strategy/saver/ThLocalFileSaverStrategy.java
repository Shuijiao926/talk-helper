package com.talkhelper.textpreprocess.strategy.saver;

import com.talkhelper.common.util.ThFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地文件保存策略
 * 将文件保存到本地临时目录
 */
@Slf4j
@Component
public class ThLocalFileSaverStrategy implements ThContentSaverStrategy {

    @Override
    public String save(String content, Object metadata) {
        try {
            if (metadata instanceof MultipartFile) {
                // 如果是上传的文件，保存到临时目录
                MultipartFile file = (MultipartFile) metadata;
                String filePath = ThFileUtils.saveToTempDir(file);
                log.info("本地文件保存成功: {}", filePath);
                return filePath;
            } else if (content != null) {
                // 如果是文本内容，可以保存到文件
                log.warn("文本内容保存需要指定文件路径，当前仅支持文件上传");
                return content;
            }
            throw new IllegalArgumentException("不支持的保存内容类型");
        } catch (Exception e) {
            log.error("本地文件保存失败", e);
            throw new RuntimeException("本地文件保存失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStrategyName() {
        return "local-file";
    }
}
