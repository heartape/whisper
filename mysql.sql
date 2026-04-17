ALTER DATABASE DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `user`;
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      phone VARCHAR(20) NOT NULL COMMENT '手机号',
                      username VARCHAR(50) NOT NULL COMMENT '用户名',
                      password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
                      avatar VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像URL',
                      bio VARCHAR(255) NOT NULL DEFAULT '' COMMENT '简介',
                      role VARCHAR(16) NOT NULL COMMENT '角色',
                      status VARCHAR(16) NOT NULL COMMENT '账号状态',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      UNIQUE KEY uk_phone (phone),
                      KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
INSERT INTO `user` VALUES (1, '12345678901', 'admin', '123456', 'https://picsum.photos/seed/u1/100', '大大怪将军', 'ADMIN', 'NORMAL', '2026-02-06 19:53:16', '2026-02-06 19:53:14');
INSERT INTO `user` VALUES (2, '12345678902', 'alan zhang', '123456', 'https://picsum.photos/seed/u2/100', '小小可怜虫', 'USER', 'NORMAL', '2026-02-06 19:53:16', '2026-02-06 19:53:14');

DROP TABLE IF EXISTS `im_session`;
CREATE TABLE im_session (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            type VARCHAR(16) NOT NULL COMMENT '会话类型',
                            name VARCHAR(100) COMMENT '会话名',
                            icon VARCHAR(255) COMMENT '会话图标',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `im_session_announcement`;
CREATE TABLE im_session_announcement (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    session_id BIGINT NOT NULL COMMENT '群会话ID，关联im_session表',
                                    user_id BIGINT NOT NULL COMMENT '公告创建者ID',
                                    content TEXT NOT NULL COMMENT '公告内容',
                                    publish_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '公告更新时间',
                                    UNIQUE KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话公告表';

DROP TABLE IF EXISTS `im_session_apply`;
CREATE TABLE im_session_apply (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            type VARCHAR(16) NOT NULL COMMENT 'FRIEND/GROUP',
                            session_id BIGINT COMMENT '群申请时必填，好友申请为空',
                            alias_name VARCHAR(32) COMMENT '好友备注',
                            applicant_id BIGINT NOT NULL COMMENT '申请人',
                            reviewer_id BIGINT NOT NULL COMMENT '审批人（好友申请为对方用户，加群申请为群主）',
                            apply_info VARCHAR(255) NOT NULL COMMENT '申请信息',
                            status VARCHAR(16) NOT NULL COMMENT '审核状态',
                            review_note VARCHAR(32) COMMENT '审核备注',
                            review_time DATETIME COMMENT '审核时间',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UNIQUE KEY idx_session_applicant_reviewer (session_id, applicant_id, reviewer_id) COMMENT '防止重复添加',
                            KEY idx_reviewer (reviewer_id, create_time DESC),
                            KEY idx_applicant (applicant_id, create_time DESC),
                            KEY idx_reviewer_status (reviewer_id, status, create_time),
                            KEY idx_session_status (session_id, status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `im_peer_session`;
CREATE TABLE im_peer_session (
                                uid1 BIGINT NOT NULL COMMENT '小User ID',
                                uid2 BIGINT NOT NULL COMMENT '大User ID',
                                session_id BIGINT NOT NULL,
                                apply_status VARCHAR(16) NOT NULL COMMENT '申请状态',
                                PRIMARY KEY uk_uid (uid1, uid2),
                                UNIQUE KEY uk_session_user (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `im_session_member`;
CREATE TABLE im_session_member (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   session_id BIGINT NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   role VARCHAR(32) NOT NULL COMMENT '群角色',
                                   alias_name VARCHAR(32) COMMENT '群内昵称',
                                   is_mute TINYINT NOT NULL DEFAULT 0 COMMENT '免打扰',
                                   is_exit TINYINT NOT NULL DEFAULT 0 COMMENT '是否已退出',
                                   is_block TINYINT NOT NULL DEFAULT 0 COMMENT '是否拉黑',
                                   unread_count INT NOT NULL DEFAULT 0 COMMENT '未读数',
                                   join_time BIGINT NOT NULL,
                                   UNIQUE KEY uk_session_user (session_id, user_id),
                                   KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `im_message`;
CREATE TABLE im_message (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            session_id BIGINT NOT NULL,
                            user_id BIGINT NOT NULL COMMENT '发送者,0为系统消息',
                            message_type VARCHAR(32) NOT NULL COMMENT '消息类型',
                            message_info TEXT NOT NULL COMMENT '信息',
                            create_time BIGINT NOT NULL COMMENT '创建时间',
                            KEY idx_session_time (session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息';

DROP TABLE IF EXISTS `system_app_version`;
CREATE TABLE system_app_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    -- 1. 平台定位
    platform VARCHAR(32) NOT NULL COMMENT '平台标识: ANDROID, IOS, WINDOWS, MAC',

    -- 2. 版本比对核心
    version_code INT NOT NULL COMMENT '内部版本号(机器比对用，单调递增，如: 10)',
    version_name VARCHAR(32) NOT NULL COMMENT '展示版本名(人类阅读用，如: "1.2.0")',
    min_compatible_code INT NOT NULL COMMENT '最低兼容版本号(低于此版本将被强制下线/升级)',

    -- 3. 升级资源
    download_url VARCHAR(512) NOT NULL COMMENT '下载包地址或App Store跳转链接',
    apk_md5 VARCHAR(64) NOT NULL COMMENT 'APK文件的MD5哈希值，用于防篡改校验',

    -- 4. 展示配置
    release_notes TEXT NOT NULL COMMENT '更新日志/发版说明(支持换行符)',
    update_strategy VARCHAR(32) NOT NULL COMMENT '升级策略: FORCE(强更), RECOMMEND(推荐), SILENT(静默)',

    -- 5. 运营发布控制
    is_published TINYINT(1) NOT NULL COMMENT '是否对外发布(0-灰度/测试中, 1-已全量发布)',
    publish_time DATETIME COMMENT '实际发布生效时间',

    -- 6. 基础审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 添加索引以加速查询
    UNIQUE KEY uk_platform_version (platform, version_code)
    KEY idx_platform_published (platform, is_published, version_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本控制表';