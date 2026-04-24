package com.talkhelper.common.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * ID生成工具类
 * 提供多种ID生成策略:UUID、雪花算法、时间戳等
 */
@Slf4j
public class ThIdGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] BASE62_CHARS = 
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * 生成UUID(无横杠)
     * 格式: 32位十六进制字符串
     * 示例: 550e8400e29b41d4a716446655440000
     *
     * @return UUID字符串(无横杠)
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成标准UUID(带横杠)
     * 格式: 8-4-4-4-12
     * 示例: 550e8400-e29b-41d4-a716-446655440000
     *
     * @return 标准UUID字符串
     */
    public static String generateStandardUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成短UUID(8位)
     * 使用Base62编码,适合URL友好场景
     * 示例: aB3xK9mN
     *
     * @return 短UUID字符串
     */
    public static String generateShortUUID() {
        long timestamp = System.currentTimeMillis();
        long random = SECURE_RANDOM.nextInt(Integer.MAX_VALUE);
        long value = (timestamp << 32) | random;
        return toBase62(value, 8);
    }

    /**
     * 生成雪花算法ID
     * 格式: 时间戳(41位) + 机器ID(10位) + 序列号(12位)
     * 特点: 趋势递增、全局唯一、高性能
     *
     * @return 雪花ID(long类型)
     */
    public static long generateSnowflakeId() {
        return SnowflakeIdWorker.getInstance().nextId();
    }

    /**
     * 生成雪花算法ID(字符串)
     *
     * @return 雪花ID字符串
     */
    public static String generateSnowflakeIdStr() {
        return String.valueOf(generateSnowflakeId());
    }

    /**
     * 生成时间戳ID
     * 格式: 毫秒时间戳 + 随机数(4位)
     * 示例: 17130240000001234
     *
     * @return 时间戳ID字符串
     */
    public static String generateTimestampId() {
        long timestamp = Instant.now().toEpochMilli();
        int random = SECURE_RANDOM.nextInt(10000);
        return String.format("%d%04d", timestamp, random);
    }

    /**
     * 生成业务ID(带前缀)
     * 格式: 前缀_时间戳_随机数
     * 示例: TASK_1713024000000_aB3x
     *
     * @param prefix 前缀
     * @return 业务ID字符串
     */
    public static String generateBusinessId(String prefix) {
        String shortId = toBase62(SECURE_RANDOM.nextInt(Integer.MAX_VALUE), 4);
        return String.format("%s_%d_%s", prefix, Instant.now().toEpochMilli(), shortId);
    }

    /**
     * 生成任务ID
     * 格式: TASK_短UUID
     * 示例: TASK_aB3xK9mN
     *
     * @return 任务ID字符串
     */
    public static String generateTaskId() {
        return "TASK_" + generateShortUUID();
    }

    /**
     * 生成订单ID
     * 格式: ORD_时间戳ID
     * 示例: ORD_17130240000001234
     *
     * @return 订单ID字符串
     */
    public static String generateOrderId() {
        return "ORD_" + generateTimestampId();
    }

    /**
     * 转换为Base62编码
     *
     * @param number 数字
     * @param length 输出长度
     * @return Base62字符串
     */
    private static String toBase62(long number, int length) {
        StringBuilder sb = new StringBuilder(length);
        // 确保number为正数,避免负数取模得到负数索引
        number = Math.abs(number);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_CHARS[(int) (number % 62)]);
            number /= 62;
        }
        return sb.reverse().toString();
    }

    /**
     * 雪花算法ID生成器(单例)
     */
    private static class SnowflakeIdWorker {
        private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker();

        // 起始时间戳 (2024-01-01 00:00:00)
        private static final long START_TIMESTAMP = 1704067200000L;

        // 机器ID所占位数
        private static final long WORKER_ID_BITS = 5L;
        // 数据中心ID所占位数
        private static final long DATA_CENTER_ID_BITS = 5L;

        // 序列号所占位数
        private static final long SEQUENCE_BITS = 12L;

        // 最大值
        private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
        private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
        private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

        // 移位偏移量
        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

        private final long workerId;
        private final long dataCenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        private SnowflakeIdWorker() {
            // 默认机器ID和数据中心ID为0
            this.workerId = 0L;
            this.dataCenterId = 0L;
        }

        public static SnowflakeIdWorker getInstance() {
            return INSTANCE;
        }

        /**
         * 生成下一个ID
         *
         * @return ID
         */
        public synchronized long nextId() {
            long timestamp = getCurrentTimestamp();

            // 时钟回拨检测
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                        String.format("时钟回拨! 上次生成ID的时间戳: %d, 当前时间戳: %d", 
                                lastTimestamp, timestamp));
            }

            // 同一毫秒内
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 序列号溢出
                if (sequence == 0) {
                    timestamp = getNextTimestamp(lastTimestamp);
                }
            } else {
                // 不同毫秒,序列号重置
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            // 组合ID
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                    | (dataCenterId << DATA_CENTER_ID_SHIFT)
                    | (workerId << WORKER_ID_SHIFT)
                    | sequence;
        }

        /**
         * 获取当前时间戳
         */
        private long getCurrentTimestamp() {
            return System.currentTimeMillis();
        }

        /**
         * 等待下一毫秒
         */
        private long getNextTimestamp(long lastTimestamp) {
            long timestamp = getCurrentTimestamp();
            while (timestamp <= lastTimestamp) {
                timestamp = getCurrentTimestamp();
            }
            return timestamp;
        }
    }
}
