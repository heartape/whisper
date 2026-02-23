package com.heartape.whisper.entity.result;

import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.entity.ImSession;
import lombok.Data;

@Data
public class ImSessionSearchResult {

    private Long id;

    private String name;

    private SessionEnum type;

    public static ImSessionSearchResult of(ImSession session){
        ImSessionSearchResult result = new ImSessionSearchResult();
        result.setId(session.getId());
        result.setName(session.getName());
        result.setType(session.getType());
        return result;
    }

}

