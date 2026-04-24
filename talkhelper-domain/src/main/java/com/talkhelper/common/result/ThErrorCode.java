package com.talkhelper.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ThErrorCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    PAYLOAD_TOO_LARGE(413, "请求体过大"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    INTERNAL_ERROR(500, "系统内部错误"),

    AUTH_LOGIN_FAILED(10001, "用户名或密码错误"),
    AUTH_TOKEN_EXPIRED(10002, "Token已过期"),
    AUTH_TOKEN_INVALID(10003, "Token无效"),
    AUTH_USER_EXISTS(10004, "用户名已存在"),
    AUTH_USER_NOT_FOUND(10005, "用户不存在"),

    TASK_NOT_FOUND(20001, "任务不存在"),
    TASK_CREATE_FAILED(20002, "任务创建失败"),
    TASK_STATUS_INVALID(20003, "任务状态不合法"),
    TASK_FILE_EMPTY(20004, "上传文件为空"),
    TASK_FILE_TOO_LARGE(20005, "上传文件过大"),

    STORAGE_UPLOAD_FAILED(30001, "文件上传失败"),
    STORAGE_DOWNLOAD_FAILED(30002, "文件下载失败"),
    STORAGE_DELETE_FAILED(30003, "文件删除失败"),

    TEXT_PARSE_FAILED(40001, "文档解析失败"),
    TEXT_CHUNK_FAILED(40002, "文本分块失败"),
    AI_PROCESS_FAILED(50001, "AI处理失败"),
    AI_CALL_LIMITED(50002, "AI调用频率超限"),

    TTS_SYNTHESIS_FAILED(60001, "TTS语音合成失败"),
    AUDIO_MERGE_FAILED(60002, "音频合并失败"),
    AUDIO_PROCESS_FAILED(60003, "音频处理失败");

    private final int code;
    private final String message;
}
