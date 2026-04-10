package com.talkhelper.common.constant;

/**
 * 日志常量类
 * 定义日志打印的标准化模板和标识
 */
public class ThLogConstants {

    // ==================== 日志分隔符 ====================
    
    /**
     * 流程开始分隔符
     */
    public static final String LOG_SEPARATOR_START = "========== 开始 {} ==========";
    
    /**
     * 流程结束分隔符
     */
    public static final String LOG_SEPARATOR_END = "========== {} 完成, 耗时: {}ms ==========";
    
    /**
     * 流程失败分隔符
     */
    public static final String LOG_SEPARATOR_ERROR = "========== {} 失败, 耗时: {}ms ==========";

    // ==================== Handler日志模板 ====================
    
    /**
     * Handler开始执行
     */
    public static final String HANDLER_START = "[{}] 开始执行";
    
    /**
     * Handler执行成功
     */
    public static final String HANDLER_SUCCESS = "[{}] 执行成功, 耗时: {}ms";
    
    /**
     * Handler执行失败
     */
    public static final String HANDLER_ERROR = "[{}] 执行失败";
    
    /**
     * Handler跳过执行
     */
    public static final String HANDLER_SKIP = "[{}] 跳过执行";

    // ==================== 业务操作日志模板 ====================
    
    /**
     * 文件操作 - 开始
     */
    public static final String FILE_OPERATION_START = "[{}] 开始{}: {}";
    
    /**
     * 文件操作 - 成功
     */
    public static final String FILE_OPERATION_SUCCESS = "[{}] {}完成: {}";
    
    /**
     * 文件处理 - 完成
     */
    public static final String FILE_PROCESS_COMPLETE = "文件处理完成: {}";
    
    /**
     * 文件处理 - 失败
     */
    public static final String FILE_PROCESS_FAILED = "文件处理失败: {}";
    
    /**
     * 文本处理 - 开始
     */
    public static final String TEXT_PROCESS_START = "[{}] 开始{}, 文本长度: {}";
    
    /**
     * 文本处理 - 成功
     */
    public static final String TEXT_PROCESS_SUCCESS = "[{}] {}完成, 结果长度: {}";
    
    /**
     * 分块操作 - 成功
     */
    public static final String CHUNK_OPERATION_SUCCESS = "[{}] {}完成, 分块数量: {}";

    // ==================== 异常日志模板 ====================
    
    /**
     * 业务执行异常
     */
    public static final String BUSINESS_EXCEPTION = "业务执行异常";
    
    /**
     * 文件处理失败（Controller层）
     */
    public static final String FILE_PROCESS_ERROR_MSG = "文件处理失败";

    // ==================== 敏感信息脱敏标识 ====================
    
    /**
     * 文件路径脱敏标识
     */
    public static final String PATH_MASKED = "***";
    
    /**
     * API Key脱敏标识
     */
    public static final String API_KEY_MASKED = "sk-***";
}
