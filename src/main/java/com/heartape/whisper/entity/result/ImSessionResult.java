package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.entity.ImSession;
import com.heartape.whisper.json.serializer.AvatarUrlSerializer;
import lombok.Data;

@Data
public class ImSessionResult {

    private Long id;

    private SessionEnum type;

    /** 会话名 */
    private String name;

    /** 群头像 */
    @JsonSerialize(using = AvatarUrlSerializer.class)
    private String icon;

    public static ImSessionResult of(ImSession imSession){
        ImSessionResult result = new ImSessionResult();
        result.setId(imSession.getId());
        result.setType(imSession.getType());
        result.setName(imSession.getName());
        result.setIcon(imSession.getIcon());
        return result;
    }
}

