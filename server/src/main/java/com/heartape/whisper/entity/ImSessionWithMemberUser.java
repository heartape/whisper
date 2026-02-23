package com.heartape.whisper.entity;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class ImSessionWithMemberUser {

    private Long id;

    private String aliasName;

    private String username;

    private String avatar;

    public String getIcon() {
        return avatar;
    }

    public String getName() {
        if (StringUtils.hasText(aliasName)){
            return aliasName;
        }else {
            return username;
        }
    }
}

