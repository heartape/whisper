package com.heartape.whisper.mapper;

import com.heartape.whisper.common.constant.GroupRoleEnum;
import com.heartape.whisper.entity.ImSessionMember;
import com.heartape.whisper.entity.ImSessionMemberUser;
import com.heartape.whisper.entity.result.UserContactsResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImSessionMemberMapper {
    List<Long> userIds(Long sessionId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId, Boolean isMute, Boolean isExit, Boolean isBlock);
    ImSessionMember selectBySessionIdAndUserId(Long sessionId, Long userId, Boolean isMute, Boolean isExit, Boolean isBlock);
    ImSessionMemberUser selectUserBySessionIdAndUserId(Long sessionId, Long userId, Boolean isMute, Boolean isExit, Boolean isBlock);

    // ImSessionMember selectBySessionIdAndNotUserId(Long sessionId, Long userId);

    int insert(ImSessionMember member);

    GroupRoleEnum selectRoleBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    List<UserContactsResult> selectUserListByUserId(Long userId);

    int insertOrUpdate(ImSessionMember member);

    List<ImSessionMember> selectListBySessionIdAndPairUserId(Long sessionId, Long uidMin, Long uidMax);

    List<ImSessionMemberUser> selectListBySessionIdAndExitAndBlock(Long sessionId, Boolean isMute, Boolean isExit, Boolean isBlock);

    int deleteBySessionIdAndUserId(Long sessionId, Long userId);

    int updateBySessionIdAndUserId(ImSessionMember value, ImSessionMember condition);

}
