package com.talkhelper.textpreprocess.service;

import com.talkhelper.common.enums.ThContentSaveStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 内容保存服务接口
 * 提供解耦的、独立的内容保存能力
 * 调用方可根据需要随时调用此服务保存文件或文本内容
 */
public interface ThContentSaveService {

    /**
     * 保存上传的文件
     *
     * @param file 上传的文件
     * @param strategies 保存策略列表（为空则使用默认策略）
     * @return 主保存路径
     */
    String saveFile(MultipartFile file, ThContentSaveStrategy... strategies);

    /**
     * 保存文本内容
     *
     * @param content 文本内容
     * @param fileName 文件名（可选，用于生成保存路径）
     * @param strategies 保存策略列表（为空则使用默认策略）
     * @return 主保存路径
     */
    String saveText(String content, String fileName, ThContentSaveStrategy... strategies);

    /**
     * 批量保存文本分块
     *
     * @param chunks 文本分块列表
     * @param baseFileName 基础文件名（用于生成保存路径）
     * @param strategies 保存策略列表（为空则使用默认策略）
     * @return 保存路径列表
     */
    List<String> saveChunks(List<String> chunks, String baseFileName, ThContentSaveStrategy... strategies);

    /**
     * 获取支持的保存策略列表
     *
     * @return 支持的策略列表
     */
    List<String> getSupportedStrategies();
}
