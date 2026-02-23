package com.heartape.whisper.mapper;

import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.entity.ImMessage;
import com.heartape.whisper.entity.ImSession;
import com.heartape.whisper.entity.ImSessionWithMemberUser;
import com.heartape.whisper.entity.result.ImSessionListResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImSessionMapper {
    List<ImSession> selectListByUserId(Long userId);

    List<ImSessionWithMemberUser> selectPeerListWithUserByNotUserId(Long userId);

    List<ImMessage> messages(Long sessionId);

    /**
     * 检查方法，用于验证指定session和用户ID的关系或权限
     *
     * @param sessionId 要检查的session
     * @param userId 用户ID
     * @return 返回一个布尔值，表示检查结果
     */
    boolean check(Long sessionId, Long userId, SessionEnum type);

    ImSessionListResult peerSession(Long sessionId, Long userId);

    ImSessionListResult groupSession(Long sessionId);

    ImSession selectById(Long sessionId);

    int insert(ImSession session);

    int touchUpdateTime(Long sessionId);

    List<ImMessage> syncMessages(@Param("sessionId") Long sessionId,
                                 @Param("beforeMessageId") Long beforeMessageId,
                                 @Param("limit") Integer limit);

    List<ImSession> selectByNameMatch(SessionEnum sessionEnum, String keyword);

}
