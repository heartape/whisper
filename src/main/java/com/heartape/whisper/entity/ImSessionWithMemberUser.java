package com.heartape.whisper.entity;

import com.heartape.whisper.util.FileUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
public class ImSessionWithMemberUser {

    private Long id;

    private String icon;

    private String name;

    // public String getIcon() {
    //     return FileUtils.icon(userId.toString());
    // }

    // public String getName() {
    //     if (StringUtils.hasText(aliasName)){
    //         return aliasName;
    //     }else {
    //         return username;
    //     }
    // }
}

