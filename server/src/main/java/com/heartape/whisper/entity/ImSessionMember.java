package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.GroupRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImSessionMember {
    private Long id;

    private Long sessionId;

    private Long userId;

    private GroupRoleEnum role;

    /** 群内昵称 */
    private String aliasName;

    /** 免打扰 */
    private Boolean isMute;

    /**
     * 是否已退出
     * todo:目前系统中peer的退出是同等退出，即a退出时触发b退出，后续需要优化，group支持管理员踢出群员和群员主动退出
     */
    private Boolean isExit;

    /**
     * 是否拉黑
     * todo:目前系统中peer的拉黑是同等拉黑，即a拉黑b时触发b拉黑a，后续需要优化，group仅支持管理员拉黑群员
     */
    private Boolean isBlock;

    /** 未读数 */
    private Integer unreadCount;

    private LocalDateTime joinTime;
}
