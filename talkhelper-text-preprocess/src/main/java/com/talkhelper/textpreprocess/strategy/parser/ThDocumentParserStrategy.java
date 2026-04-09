package com.talkhelper.textpreprocess.strategy.parser;

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
