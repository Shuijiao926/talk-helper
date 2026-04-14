package com.talkhelper.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件操作工具类
 * 提供文件保存、删除等常用操作
 */
@Slf4j
public class ThFileUtils {

    /**
     * 默认临时目录
     */
    private static final String DEFAULT_TEMP_DIR = System.getProperty("java.io.tmpdir") + "/talkhelper/uploads/";

    /**
     * 保存上传的文件到临时目录
     *
     * @param file 上传的文件
     * @return 保存后的文件路径
     * @throws IOException IO异常
     */
    public static String saveToTempDir(MultipartFile file) throws IOException {
        return saveToTempDir(file, DEFAULT_TEMP_DIR);
    }

    /**
     * 保存上传的文件到指定目录
     *
     * @param file      上传的文件
     * @param targetDir 目标目录
     * @return 保存后的文件路径
     * @throws IOException IO异常
     */
    public static String saveToTempDir(MultipartFile file, String targetDir) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 创建目标目录
        File dir = new File(targetDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("创建目录: {}", targetDir);
        }

        // 生成唯一文件名
        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());

        // 保存文件
        Path filePath = Paths.get(targetDir + uniqueFilename);
        Files.write(filePath, file.getBytes());

        log.info("文件保存成功: {}", filePath);
        return filePath.toString();
    }

    /**
     * 生成唯一的文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名（UUID + 扩展名）
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = extractFileExtension(originalFilename);
        return UUID.randomUUID() + extension;
    }

    /**
     * 提取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名（包含点号，如 .txt），如果没有扩展名则返回空字符串
     */
    public static String extractFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    /**
     * 获取文件类型（扩展名，不包含点号）
     *
     * @param filename 文件名
     * @return 文件类型（如 txt、pdf），如果没有扩展名则返回空字符串
     */
    public static String getFileType(String filename) {
        String extension = extractFileExtension(filename);
        return extension.isEmpty() ? "" : extension.substring(1).toLowerCase();
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                } else {
                    log.warn("文件删除失败: {}", filePath);
                }
                return deleted;
            }
            return true; // 文件不存在视为删除成功
        } catch (Exception e) {
            log.error("删除文件异常: {}", filePath, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean isFileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return new File(filePath).exists();
    }

    /**
     * 获取文件大小（字节）
     *
     * @param filePath 文件路径
     * @return 文件大小，如果文件不存在返回 -1
     */
    public static long getFileSize(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return -1;
        }

        File file = new File(filePath);
        if (file.exists()) {
            return file.length();
        }
        return -1;
    }

    /**
     * 确保目录存在，如果不存在则创建
     *
     * @param dirPath 目录路径
     * @return 目录是否存在或创建成功
     */
    public static boolean ensureDirectoryExists(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            return false;
        }

        File dir = new File(dirPath);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return dir.isDirectory();
    }

    /**
     * 将MultipartFile保存到临时文件
     *
     * @param file 上传的文件
     * @return 临时文件路径
     * @throws Exception 异常
     */
    public static Path saveToTempFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = extractFileExtension(originalFilename);
        if (suffix.isEmpty()) {
            suffix = ".tmp";
        }

        Path tempFile = Files.createTempFile("talkhelper_", suffix);
        file.transferTo(tempFile.toFile());

        log.debug("临时文件创建成功: {}, 大小: {} bytes", tempFile, file.getSize());

        return tempFile;
    }
}
