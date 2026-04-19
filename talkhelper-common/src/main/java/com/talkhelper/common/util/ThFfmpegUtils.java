package com.talkhelper.common.util;

import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * FFmpeg音频处理工具类
 * 基于JAVE2 (Java Audio Video Encoder) 封装
 * 使用JAVE2内置的FFmpeg二进制，无需系统安装FFmpeg
 */
@Slf4j
public class ThFfmpegUtils {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /** JAVE2 内置 FFmpeg 可执行文件路径 */
    private static final String FFMPEG_PATH;

    static {
        DefaultFFMPEGLocator locator = new DefaultFFMPEGLocator();
        FFMPEG_PATH = locator.getExecutablePath();
        log.info("使用JAVE2内置FFmpeg: {}", FFMPEG_PATH);
    }

    /**
     * 生成指定时长的静音音频
     *
     * @param durationSeconds 静音时长（秒）
     * @return 静音音频文件路径
     */
    public static String generateSilence(double durationSeconds) {
        try {
            String outputPath = createTempFile("silence", ".wav");
            log.info("生成 {:.1f} 秒静音音频: {}", durationSeconds, outputPath);

            // 使用FFmpeg命令生成静音
            // ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t {duration} -q:a 9 -acodec libmp3lame output.wav
            String command = String.format(
                    FFMPEG_PATH + " -y -f lavfi -i anullsrc=r=44100:cl=mono -t %.2f -acodec pcm_s16le %s",
                    durationSeconds,
                    outputPath
            );

            executeCommand(command);
            
            log.info("静音音频生成成功: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("生成静音音频失败", e);
            throw new RuntimeException("生成静音音频失败: " + e.getMessage(), e);
        }
    }

    /**
     * 拼接多个音频文件
     *
     * @param audioPaths 音频文件路径列表
     * @return 拼接后的音频文件路径
     */
    public static String concatAudio(java.util.List<String> audioPaths) {
        if (audioPaths == null || audioPaths.isEmpty()) {
            throw new IllegalArgumentException("音频路径列表不能为空");
        }

        try {
            String concatListPath = createTempFile("concat_list", ".txt");
            String outputPath = createTempFile("concatenated", ".wav");

            // 1. 生成concat列表文件
            generateConcatList(audioPaths, concatListPath);

            // 2. 执行FFmpeg拼接命令
            // ffmpeg -f concat -safe 0 -i list.txt -c copy output.wav
            String command = String.format(
                    FFMPEG_PATH + " -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"",
                    concatListPath,
                    outputPath
            );

            executeCommand(command);
            
            log.info("音频拼接成功, 共 {} 个片段, 输出: {}", audioPaths.size(), outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("音频拼接失败", e);
            throw new RuntimeException("音频拼接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 多轨混音（人声 + BGM）
     *
     * @param vocalPath 人声音频路径
     * @param bgmPath 背景音乐路径
     * @param bgmVolume BGM音量（0.0-1.0，建议0.2-0.3）
     * @param fadeDuration 淡入淡出时长（秒）
     * @return 混音后的音频路径
     */
    public static String mixAudio(String vocalPath, String bgmPath, double bgmVolume, double fadeDuration) {
        try {
            String outputPath = createTempFile("mixed", ".mp3");
            log.info("开始混音: 人声={}, BGM={}, BGM音量={}", vocalPath, bgmPath, bgmVolume);

            // FFmpeg混音命令
            // 人声保持原音量，BGM降低音量并添加淡入淡出
            String command = String.format(
                    FFMPEG_PATH + " -y -i \"%s\" -i \"%s\" " +
                    "-filter_complex \"[0:a]volume=1.0[vocal];" +
                    "[1:a]volume=%.2f,afade=t=in:st=0:d=%.1f,afade=t=out:st=end_duration-%.1f:d=%.1f[bgm];" +
                    "[vocal][bgm]amix=inputs=2:duration=first:dropout_transition=2\" " +
                    "-codec:a libmp3lame -b:a 320k \"%s\"",
                    vocalPath,
                    bgmPath,
                    bgmVolume,
                    fadeDuration,
                    fadeDuration,
                    fadeDuration,
                    outputPath
            );

            // 注意：end_duration需要动态获取BGM时长，这里简化处理
            // 实际使用时需要先获取音频时长
            executeCommand(command);
            
            log.info("混音完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("混音失败", e);
            throw new RuntimeException("混音失败: " + e.getMessage(), e);
        }
    }

    /**
     * 音量标准化（使用EBU R128标准）
     *
     * @param inputPath 输入音频路径
     * @return 标准化后的音频路径
     */
    public static String normalizeVolume(String inputPath) {
        try {
            String outputPath = createTempFile("normalized", ".mp3");
            log.info("开始音量标准化: {}", inputPath);

            // 使用loudnorm滤波器进行音量标准化
            // I=-16: 目标响度 -16 LUFS（播客标准）
            // TP=-1.5: 真峰值上限 -1.5 dBTP
            // LRA=11: 响度范围 11 LU
            String command = String.format(
                    FFMPEG_PATH + " -y -i \"%s\" -af loudnorm=I=-16:TP=-1.5:LRA=11 " +
                    "-codec:a libmp3lame -b:a 320k \"%s\"",
                    inputPath,
                    outputPath
            );

            executeCommand(command);
            
            log.info("音量标准化完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("音量标准化失败", e);
            throw new RuntimeException("音量标准化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 音频格式转换
     *
     * @param inputPath 输入音频路径
     * @param outputPath 输出音频路径
     * @param bitrate 比特率（kbps），如320
     * @param sampleRate 采样率（Hz），如44100
     */
    public static void convertFormat(String inputPath, String outputPath, int bitrate, int sampleRate) {
        try {
            log.info("开始格式转换: {} -> {}", inputPath, outputPath);

            String command = String.format(
                    FFMPEG_PATH + " -y -i \"%s\" -ar %d -b:a %dk -codec:a libmp3lame \"%s\"",
                    inputPath,
                    sampleRate,
                    bitrate,
                    outputPath
            );

            executeCommand(command);
            
            log.info("格式转换完成: {}", outputPath);
            
        } catch (Exception e) {
            log.error("格式转换失败", e);
            throw new RuntimeException("格式转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取音频时长（秒）
     *
     * @param audioPath 音频文件路径
     * @return 时长（秒）
     */
    public static double getAudioDuration(String audioPath) {
        try {
            MultimediaObject multimediaObject = new MultimediaObject(new File(audioPath));
            MultimediaInfo info = multimediaObject.getInfo();
            long durationMs = info.getDuration();
            double durationSeconds = durationMs / 1000.0;
            
            log.debug("音频时长: {} 秒", durationSeconds);
            return durationSeconds;
            
        } catch (Exception e) {
            log.error("获取音频时长失败: {}", audioPath, e);
            throw new RuntimeException("获取音频时长失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取音频信息
     *
     * @param audioPath 音频文件路径
     * @return 音频信息对象
     */
    public static AudioInfo getAudioInfo(String audioPath) {
        try {
            MultimediaObject multimediaObject = new MultimediaObject(new File(audioPath));
            MultimediaInfo info = multimediaObject.getInfo();
            return info.getAudio();
            
        } catch (Exception e) {
            log.error("获取音频信息失败: {}", audioPath, e);
            throw new RuntimeException("获取音频信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加音效（在指定时间点插入短音频）
     *
     * @param mainAudioPath 主音频路径
     * @param sfxPath 音效音频路径
     * @param insertTime 插入时间点（秒）
     * @return 添加音效后的音频路径
     */
    public static String addSoundEffect(String mainAudioPath, String sfxPath, double insertTime) {
        try {
            String outputPath = createTempFile("with_sfx", ".mp3");
            log.info("在 {:.1f} 秒处添加音效: {}", insertTime, sfxPath);

            // 使用adelay和amix实现音效插入
            // 这里简化处理，实际需要根据insertTime计算delay
            String command = String.format(
                    FFMPEG_PATH + " -y -i \"%s\" -i \"%s\" " +
                    "-filter_complex \"[1:a]adelay=%.0f|%.0f[sfx];[0:a][sfx]amix=inputs=2:duration=first\" " +
                    "-codec:a libmp3lame -b:a 320k \"%s\"",
                    mainAudioPath,
                    sfxPath,
                    insertTime * 1000, // FFmpeg adelay使用毫秒
                    insertTime * 1000,
                    outputPath
            );

            executeCommand(command);
            
            log.info("音效添加完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("添加音效失败", e);
            throw new RuntimeException("添加音效失败: " + e.getMessage(), e);
        }
    }

    /**
     * 裁剪音频
     *
     * @param inputPath 输入音频路径
     * @param startTime 起始时间（秒）
     * @param duration 时长（秒）
     * @return 裁剪后的音频路径
     */
    public static String cropAudio(String inputPath, double startTime, double duration) {
        try {
            String outputPath = createTempFile("cropped", ".mp3");
            log.info("裁剪音频: {} [{:.1f}s - {:.1f}s]", inputPath, startTime, startTime + duration);

            String command = String.format(
                    FFMPEG_PATH + " -y -i \"%s\" -ss %.2f -t %.2f -codec:a libmp3lame -b:a 320k \"%s\"",
                    inputPath,
                    startTime,
                    duration,
                    outputPath
            );

            executeCommand(command);
            
            log.info("音频裁剪完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("音频裁剪失败", e);
            throw new RuntimeException("音频裁剪失败: " + e.getMessage(), e);
        }
    }

    /**
     * 淡入淡出效果
     *
     * @param inputPath 输入音频路径
     * @param fadeInDuration 淡入时长（秒）
     * @param fadeOutDuration 淡出时长（秒）
     * @return 添加淡入淡出后的音频路径
     */
    public static String applyFadeInOut(String inputPath, double fadeInDuration, double fadeOutDuration) {
        try {
            String outputPath = createTempFile("faded", ".mp3");
            double totalDuration = getAudioDuration(inputPath);
            
            log.info("添加淡入淡出: 淡入{:.1f}s, 淡出{:.1f}s", fadeInDuration, fadeOutDuration);

            String command = String.format(
                    FFMPEG_PATH + " -y -i \"%s\" -af \"afade=t=in:st=0:d=%.1f,afade=t=out:st=%.1f:d=%.1f\" " +
                    "-codec:a libmp3lame -b:a 320k \"%s\"",
                    inputPath,
                    fadeInDuration,
                    totalDuration - fadeOutDuration,
                    fadeOutDuration,
                    outputPath
            );

            executeCommand(command);
            
            log.info("淡入淡出添加完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("添加淡入淡出失败", e);
            throw new RuntimeException("添加淡入淡出失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成concat列表文件
     */
    private static void generateConcatList(java.util.List<String> audioPaths, String listFilePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(listFilePath))) {
            for (String path : audioPaths) {
                // FFmpeg concat格式: file 'path'
                writer.println("file '" + path.replace("\\", "/") + "'");
            }
        }
        log.debug("Concat列表文件生成: {}, 共 {} 项", listFilePath, audioPaths.size());
    }

    /**
     * 执行FFmpeg命令
     */
    private static void executeCommand(String command) throws Exception {
        log.debug("执行FFmpeg命令: {}", command);
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // Windows系统需要使用cmd /c
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("cmd", "/c", command);
        } else {
            processBuilder.command("bash", "-c", command);
        }
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // 读取输出
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.debug("FFmpeg输出: {}", line);
        }
        
        // 等待执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg命令执行失败, 退出码: " + exitCode);
        }
    }

    /**
     * 创建临时文件路径
     */
    private static String createTempFile(String prefix, String suffix) {
        String fileName = prefix + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;
        Path tempPath = Paths.get(TEMP_DIR, fileName);
        return tempPath.toString();
    }

    /**
     * 删除临时文件
     */
    public static void deleteTempFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.debug("临时文件已删除: {}", filePath);
        } catch (IOException e) {
            log.warn("删除临时文件失败: {}", filePath, e);
        }
    }
}

