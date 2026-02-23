package com.heartape.whisper.mapper;

import com.heartape.whisper.common.constant.GroupRoleEnum;
import com.heartape.whisper.entity.ImSessionMember;
import com.heartape.whisper.entity.ImSessionMemberUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImSessionMemberMapper {
    List<Long> userIds(Long sessionId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId, Boolean isMute, Boolean isExit, Boolean isBlock);
    ImSessionMember selectBySessionIdAndUserId(Long sessionId, Long userId, Boolean isMute, Boolean isExit, Boolean isBlock);

    ImSessionMember selectBySessionIdAndNotUserId(Long sessionId, Long userId);

    int insert(ImSessionMember member);

    int resetUnread(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    boolean exists(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    int updateRole(@Param("sessionId") Long sessionId, @Param("userId") Long userId, @Param("role") String role);

    int updateMuteBySessionIdAndUserId(Long sessionId, Long userId, boolean isMute);

    int updateKickBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    int recoverMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    GroupRoleEnum selectRoleBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    int insertOrUpdate(ImSessionMember member);

    List<ImSessionMember> selectListBySessionIdAndPairUserId(Long sessionId, Long uidMin, Long uidMax);

    List<ImSessionMemberUser> selectListBySessionIdAndExitAndBlock(Long sessionId, Boolean isExit, Boolean isBlock);

    void updateBlockAndKickBySessionIdAndUserId(Long sessionId, Long userId);

    void updateBlockBySessionIdAndUserId(Long sessionId, Long userId, boolean isBlock);

    int deleteBySessionIdAndUserId(Long sessionId, Long userId);

    int updateBySessionIdAndUserId(ImSessionMember value, ImSessionMember condition);

    int updateBySessionIdAndNotUserId(ImSessionMember value, ImSessionMember condition);
}
