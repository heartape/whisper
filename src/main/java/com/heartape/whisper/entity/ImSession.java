package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.SessionEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImSession {

    private Long id;

    private SessionEnum type;

    /** 会话名 */
    private String name;

    /** 群头像 */
    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

