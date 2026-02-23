ALTER DATABASE DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `user`;
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      phone VARCHAR(20) NOT NULL COMMENT '手机号',
                      username VARCHAR(50) NOT NULL COMMENT '用户名',
                      password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
                      avatar VARCHAR(255) DEFAULT '' COMMENT '头像URL',
                      bio VARCHAR(255) DEFAULT '' COMMENT '简介',
                      role VARCHAR(16) NOT NULL COMMENT '角色',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      UNIQUE KEY uk_phone (phone),
                      KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
INSERT INTO `user` VALUES (1, '12345678901', 'admin', '123456', 'https://picsum.photos/seed/detail/100', '大大怪将军', 'ADMIN', '2026-02-06 19:53:16', '2026-02-06 19:53:14');
INSERT INTO `user` VALUES (2, '12345678902', 'alan zhang', '123456', 'https://picsum.photos/seed/detail/101', '小小可怜虫', 'USER', '2026-02-06 19:53:16', '2026-02-06 19:53:14');

DROP TABLE IF EXISTS `follow`;
CREATE TABLE follow (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        follower_id BIGINT NOT NULL COMMENT '关注者（我）',
                        followee_id BIGINT NOT NULL COMMENT '被关注者（对方）',
                        follow_type VARCHAR(16) NOT NULL COMMENT '关注类型',
                        muted TINYINT NOT NULL DEFAULT 0 COMMENT '是否静默',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_follow (follower_id, followee_id),
                        KEY idx_follower (follower_id, create_time),
                        KEY idx_followee (followee_id, create_time)
) ENGINE=InnoDB COMMENT='用户关注关系表';

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
                            biz_type VARCHAR(16) NOT NULL COMMENT 'FRIEND/GROUP',
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
                                   alias_name VARCHAR(32) NOT NULL COMMENT '群内昵称',
                                   is_mute TINYINT NOT NULL DEFAULT 0 COMMENT '免打扰',
                                   is_exit TINYINT NOT NULL DEFAULT 0 COMMENT '是否已退出',
                                   is_block TINYINT NOT NULL DEFAULT 0 COMMENT '是否拉黑',
                                   unread_count INT NOT NULL DEFAULT 0 COMMENT '未读数',
                                   join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            KEY idx_session_time (session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息';
