package com.talkhelper.textpreprocess.strategy.parser;

import java.io.InputStream;

/**
 * 文档解析器策略接口
 * 使用策略模式,支持不同格式文档的解析
 */
public interface ThDocumentParserStrategy {

    /**
     * 解析文档,提取纯文本
     *
     * @param filePath 文件路径
     * @return 提取的纯文本内容
     */
    String parse(String filePath);

    /**
     * 解析文档,提取纯文本（从输入流）
     * 默认实现：将输入流保存到临时文件后调用 parse(filePath)
     * 子类可重写以提供更高效的实现
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名（用于确定文件类型）
     * @return 提取的纯文本内容
     */
    default String parse(InputStream inputStream, String fileName) {
        throw new UnsupportedOperationException(
            "当前解析器不支持直接从输入流解析，请先保存文件: " + getSupportedFileType());
    }

    /**
     * 获取支持的文件类型
     *
     * @return 文件类型标识 (txt, md, html, pdf, docx, epub)
     */
    String getSupportedFileType();

    /**
     * 判断是否支持该文件类型
     *
     * @param fileType 文件类型
     * @return 是否支持
     */
    default boolean supports(String fileType) {
        return getSupportedFileType().equalsIgnoreCase(fileType);
    }
}
