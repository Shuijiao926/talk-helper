package com.talkhelper.common.constant;

/**
 * 系统常量类
 */
public class ThConstants {

    // ==================== HTTP状态码 ====================
    
    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    // ==================== 业务状态 ====================
    
    /**
     * 处理状态 - 成功
     */
    public static final String STATUS_SUCCESS = "success";
    
    /**
     * 处理状态 - 失败
     */
    public static final String STATUS_FAILED = "failed";
    
    /**
     * 未知标识
     */
    public static final String UNKNOWN = "unknown";

    // ==================== 文本处理默认配置 ====================
    
    /**
     * 默认分块大小（字符数）
     */
    public static final int DEFAULT_CHUNK_SIZE = 2000;
    
    /**
     * 默认重叠大小（字符数）
     */
    public static final int DEFAULT_OVERLAP_SIZE = 200;
    
    /**
     * 默认最大分块大小（字符数）
     */
    public static final int DEFAULT_MAX_CHUNK_SIZE = 1000;
    
    /**
     * 句子边界搜索范围（字符数）
     */
    public static final int SENTENCE_BOUNDARY_SEARCH_RANGE = 200;

    // ==================== 文件处理 ====================
    
    /**
     * 默认文件类型标识
     */
    public static final String DEFAULT_FILE_TYPE = "text";
    
    /**
     * 默认输入文件名
     */
    public static final String DEFAULT_INPUT_FILENAME = "text_input";

    // ==================== 日志动作名称 ====================
    
    /**
     * 动作名称 - 文件保存
     */
    public static final String ACTION_FILE_SAVE = "文件保存";
    
    /**
     * 动作名称 - 文本保存
     */
    public static final String ACTION_TEXT_SAVE = "文本保存";
    
    /**
     * 动作名称 - 分块文本保存
     */
    public static final String ACTION_CHUNK_TEXT_SAVE = "分块文本保存";
    
    /**
     * 动作名称 - 文档解析
     */
    public static final String ACTION_DOCUMENT_PARSE = "文档解析";
    
    /**
     * 动作名称 - 文本清洗
     */
    public static final String ACTION_TEXT_CLEAN = "文本清洗";
    
    /**
     * 动作名称 - 文本分块
     */
    public static final String ACTION_TEXT_CHUNK = "文本分块";
    
    /**
     * 动作名称 - AI处理
     */
    public static final String ACTION_AI_PROCESS = "AI处理";
    
    /**
     * 动作名称 - 结果构建
     */
    public static final String ACTION_RESULT_BUILD = "结果构建";
}
