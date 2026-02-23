package com.heartape.whisper.entity.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ImMessageSimpleResult {

    private String aliasName;

    private String messageInfo;

    private LocalDateTime createTime;

}

