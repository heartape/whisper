package com.heartape.whisper.entity.result;

import com.heartape.whisper.common.constant.SessionEnum;
import lombok.Data;

@Data
public class ImSessionListResult {

    private Long id;

    private String name;

    private SessionEnum type;

}

