package com.heartape.whisper.service;

import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.entity.ImSessionMemberUser;
import com.heartape.whisper.entity.Param.*;
import com.heartape.whisper.entity.result.*;

import java.util.List;

public interface ImService {
    void applyPeerSession(PeerSessionApplyParam param);
    void reviewApply(Long userId, ApplyReviewParam applyReviewParam);
    Long createGroupSession(GroupSessionParam groupSessionParam);
    void applyGroupSession(GroupSessionApplyParam groupSessionApplyParam);
    void managePeerMember(PeerMemberManageParam param);
    void manageGroupMember(GroupMemberManageParam param);
    List<ImSessionResult> sessions(Long userId);

    List<UserContactsResult> contacts(Long userId);

    List<ImSessionSearchResult> sessions(SessionEnum type, String keyword);

    /**
     * 在用户消息是以每个用户一个box（即写扩散时），以用户维度拉取消息会非常有效
     */
    List<ImMessageResult> messages(Integer count, Long since, Long before);
    List<ImMessageResult> messages(Long sessionId, Integer count, Long since, Long before);
    List<ImMessageResult> syncMessages(Long userId, Long sessionId, MessageSyncParam messageSyncParam);
    void send(ImMessageParam messageParam);

    List<ImSessionMemberUser> members(Long sessionId);

    ImSessionMemberUser member(Long sessionId, Long userId);

    ImSessionResult session(Long sessionId);

    List<ImSessionApplyResult> pendingApplies(Long userId);

    List<ImSessionApplyResult> sentApplies(Long userId);

    ImSessionMemberUser alasSessionMember(SessionAliasParam param);

    void exit(PeerSessionExitParam param);

    void publishGroupAnnouncement(GroupAnnouncementParam param);

    ImSessionAnnouncementResult announcement(Long sessionId, Long userId);

}
