-- 任务表
CREATE TABLE IF NOT EXISTS `th_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID(自增)',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID(UUID)',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    `task_type` VARCHAR(32) NOT NULL COMMENT '任务类型:text-preprocess-文本预处理,podcast-generate-播客生成',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    `progress` INT DEFAULT 0 COMMENT '进度百分比',
    `current_stage` VARCHAR(255) DEFAULT NULL COMMENT '当前阶段描述',
    `request_data_url` VARCHAR(512) DEFAULT NULL COMMENT '请求参数JSON的对象存储URL',
    `result_data_url` VARCHAR(512) DEFAULT NULL COMMENT '结果数据JSON的对象存储URL',
    `error_message` TEXT COMMENT '错误信息',
    
    -- 输入文件信息
    `input_file_name` VARCHAR(255) DEFAULT NULL COMMENT '输入文件名',
    `input_file_url` VARCHAR(512) DEFAULT NULL COMMENT '输入文件MinIO URL',
    `input_file_size` BIGINT DEFAULT NULL COMMENT '输入文件大小(字节)',
    
    -- 输出文件信息
    `output_file_name` VARCHAR(255) DEFAULT NULL COMMENT '输出文件名',
    `output_file_url` VARCHAR(512) DEFAULT NULL COMMENT '输出文件MinIO URL',
    `output_file_size` BIGINT DEFAULT NULL COMMENT '输出文件大小(字节)',
    
    `start_time` DATETIME DEFAULT NULL COMMENT '开始处理时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步任务表';


CREATE TABLE IF NOT EXISTS `th_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID(自增)',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID(UUID)',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status` VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '用户状态:active-激活,disabled-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


CREATE TABLE IF NOT EXISTS `th_podcast` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID(自增)',
    `podcast_id` VARCHAR(64) NOT NULL COMMENT '播客ID(UUID)',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '播客标题',
    `description` TEXT COMMENT '播客描述',
    `cover_url` VARCHAR(512) DEFAULT NULL COMMENT '封面图片URL',
    `audio_url` VARCHAR(512) DEFAULT NULL COMMENT '最终音频文件URL(对象存储)',
    `audio_duration` INT DEFAULT NULL COMMENT '音频时长(秒)',
    `audio_file_size` BIGINT DEFAULT NULL COMMENT '音频文件大小(字节)',
    `status` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT '状态:draft-草稿,generating-生成中,published-已发布,archived-已归档,failed-生成失败',
    `visibility` VARCHAR(16) NOT NULL DEFAULT 'private' COMMENT '可见性:private-私有,public-公开',
    `original_text_path` VARCHAR(512) DEFAULT NULL COMMENT '原始文本文件路径(对象存储)',
    `original_file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_podcast_id` (`podcast_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_visibility` (`visibility`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播客表';

CREATE TABLE IF NOT EXISTS `th_podcast_script` (
                                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID(自增)',
                                                   `script_id` VARCHAR(64) NOT NULL COMMENT '脚本ID(UUID)',
    `podcast_id` VARCHAR(64) NOT NULL COMMENT '关联播客ID',
    `version` INT NOT NULL DEFAULT 1 COMMENT '脚本版本号',
    `is_current` TINYINT NOT NULL DEFAULT 1 COMMENT '是否当前版本:0-否,1-是',
    `content_url` VARCHAR(512) NOT NULL COMMENT '脚本内容JSON的对象存储URL',
    `content_plain` TEXT COMMENT '脚本纯文本(用于预览)',
    `generate_source` VARCHAR(32) NOT NULL COMMENT '生成来源:AI-自动生成,USER-用户编辑',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_script_id` (`script_id`),
    KEY `idx_podcast_id` (`podcast_id`),
    KEY `idx_podcast_id_version` (`podcast_id`, `version`),
    KEY `idx_is_current` (`is_current`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播客脚本表';


CREATE TABLE IF NOT EXISTS `th_material` (
                                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID(自增)',
                                             `material_id` VARCHAR(64) NOT NULL COMMENT '素材ID(UUID)',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '上传用户ID(系统素材为NULL)',
    `type` VARCHAR(32) NOT NULL COMMENT '素材类型:BGM-背景音乐,SOUND_EFFECT-音效,COVER-封面',
    `name` VARCHAR(255) NOT NULL COMMENT '素材名称',
    `description` TEXT COMMENT '素材描述',
    `file_url` VARCHAR(512) NOT NULL COMMENT '素材文件URL(对象存储)',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `duration` INT DEFAULT NULL COMMENT '音频时长(秒,仅音频素材)',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '分类:轻快/悬疑/严肃/悲伤等',
    `tags` VARCHAR(512) DEFAULT NULL COMMENT '标签(逗号分隔)',
    `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开:0-私有,1-公开',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_material_id` (`material_id`),
    KEY `idx_type` (`type`),
    KEY `idx_category` (`category`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_public` (`is_public`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材表';


CREATE TABLE IF NOT EXISTS `th_tts_character` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID(自增)',
                                                  `character_id` VARCHAR(64) NOT NULL COMMENT '角色ID(UUID)',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '创建用户ID(系统角色为NULL)',
    `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `description` TEXT COMMENT '角色描述',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '角色头像URL',
    `tts_provider` VARCHAR(32) NOT NULL COMMENT 'TTS服务商:TONGYI-通义千问,DOUBAO-豆包,OPENAI-OpenAI',
    `voice_id` VARCHAR(128) NOT NULL COMMENT '服务商音色ID',
    `voice_type` VARCHAR(32) DEFAULT NULL COMMENT '音色类型:FEMALE-女声,MALE-男声,CHILD-童声',
    `default_speed` DECIMAL(3,1) DEFAULT 1.0 COMMENT '默认语速(0.5-2.0)',
    `default_pitch` DECIMAL(3,1) DEFAULT 1.0 COMMENT '默认音调(0.5-2.0)',
    `is_cloned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否克隆音色:0-否,1-是',
    `clone_sample_url` VARCHAR(512) DEFAULT NULL COMMENT '克隆样本音频URL',
    `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开:0-私有,1-公开',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_character_id` (`character_id`),
    KEY `idx_tts_provider` (`tts_provider`),
    KEY `idx_voice_type` (`voice_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_public` (`is_public`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TTS角色表';
