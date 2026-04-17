package com.heartape.whisper.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImSessionAnnouncement {
    private Long id;
    private Long sessionId;
    private Long userId;
    private String content;
    private LocalDateTime publishTime;
}
