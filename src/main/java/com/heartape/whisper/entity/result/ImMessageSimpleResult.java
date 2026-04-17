package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.json.serializer.LocalDateTimeToTimestampSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ImMessageSimpleResult {

    private String aliasName;

    private String messageInfo;

    @JsonSerialize(using = LocalDateTimeToTimestampSerializer.class)
    private LocalDateTime createTime;

}

