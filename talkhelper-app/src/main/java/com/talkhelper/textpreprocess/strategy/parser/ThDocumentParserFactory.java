package com.talkhelper.textpreprocess.strategy.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档解析器工厂
 * 使用工厂模式,根据文件类型获取对应的解析器
 */
@Slf4j
@Component
public class ThDocumentParserFactory {

    private final Map<String, ThDocumentParserStrategy> parserStrategyMap;

    public ThDocumentParserFactory(List<ThDocumentParserStrategy> parsers) {
        // 将所有解析器策略注册到Map中,key为文件类型
        this.parserStrategyMap = parsers.stream()
                .collect(Collectors.toMap(
                        ThDocumentParserStrategy::getSupportedFileType,
                        parser -> parser
                ));
        log.info("文档解析器工厂初始化完成, 支持的格式: {}", parserStrategyMap.keySet());
    }

    /**
     * 根据文件类型获取解析器
     *
     * @param fileType 文件类型
     * @return 对应的解析器策略
     */
    public ThDocumentParserStrategy getParser(String fileType) {
        ThDocumentParserStrategy parser = parserStrategyMap.get(fileType.toLowerCase());
        if (parser == null) {
            throw new UnsupportedOperationException("不支持的文件类型: " + fileType);
        }
        return parser;
    }

    /**
     * 判断是否支持该文件类型
     *
     * @param fileType 文件类型
     * @return 是否支持
     */
    public boolean isSupported(String fileType) {
        return parserStrategyMap.containsKey(fileType.toLowerCase());
    }

    /**
     * 获取所有支持的文件类型
     *
     * @return 支持的文件类型列表
     */
    public List<String> getSupportedFileTypes() {
        return List.copyOf(parserStrategyMap.keySet());
    }
}
