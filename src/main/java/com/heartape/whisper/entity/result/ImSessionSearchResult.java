package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.entity.ImSession;
import com.heartape.whisper.json.serializer.AvatarUrlSerializer;
import lombok.Data;

@Data
public class ImSessionSearchResult {

    private Long id;

    private String name;

    @JsonSerialize(using = AvatarUrlSerializer.class)
    private String icon;

    private SessionEnum type;

    public static ImSessionSearchResult of(ImSession session){
        ImSessionSearchResult result = new ImSessionSearchResult();
        result.setId(session.getId());
        result.setName(session.getName());
        result.setIcon(session.getIcon());
        result.setType(session.getType());
        return result;
    }

}

