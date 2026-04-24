package com.talkhelper.textpreprocess.service;

import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文本预处理服务接口
 */
public interface ThTextPreprocessService {

    /**
     * 上传并处理文件
     *
     * @param file 上传的文件
     * @return 预处理结果
     */
    ThPreprocessResultVO uploadAndProcess(MultipartFile file);

    /**
     * 上传并处理文件(带配置)
     *
     * @param request 上传请求
     * @return 预处理结果
     */
    ThPreprocessResultVO uploadAndProcessWithConfig(ThFileUploadRequest request);

    /**
     * 获取支持的文件类型
     *
     * @return 支持的文件类型列表
     */
    String[] getSupportedFileTypes();
}
