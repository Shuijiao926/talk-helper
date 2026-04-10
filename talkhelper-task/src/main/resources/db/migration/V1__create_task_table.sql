-- 任务表
CREATE TABLE IF NOT EXISTS `th_task` (
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    `task_type` VARCHAR(32) NOT NULL COMMENT '任务类型',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    `progress` INT DEFAULT 0 COMMENT '进度百分比',
    `current_stage` VARCHAR(255) DEFAULT NULL COMMENT '当前阶段描述',
    `request_data` TEXT COMMENT '请求参数JSON',
    `result_data` LONGTEXT COMMENT '结果数据JSON',
    `error_message` TEXT COMMENT '错误信息',
    `original_file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始处理时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步任务表';
