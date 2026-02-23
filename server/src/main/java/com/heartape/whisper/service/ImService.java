package com.heartape.whisper.service;

import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.entity.ImMessage;
import com.heartape.whisper.entity.ImSessionAnnouncement;
import com.heartape.whisper.entity.ImSessionApply;
import com.heartape.whisper.entity.ImSessionMemberUser;
import com.heartape.whisper.entity.Param.*;
import com.heartape.whisper.entity.result.*;

import java.util.List;

public interface ImService {
    void applyPeerSession(PeerSessionApplyParam param);
    void reviewApply(Long userId, ApplyReviewParam applyReviewParam);
    Long createGroupSession(GroupSessionParam groupSessionParam);
    void applyGroupSession(GroupSessionApplyParam groupSessionApplyParam);
    void manageGroupMember(GroupMemberManageParam param);
    List<ImSessionResult> sessions(Long userId);

    List<ImSessionSearchResult> sessions(SessionEnum sessionEnum, String keyword);
    List<ImMessage> messages(Long sessionId);
    List<ImMessage> syncMessages(Long userId, Long sessionId, MessageSyncParam messageSyncParam);
    ImMessageResult send(ImMessageParam messageParam);

    List<ImSessionMemberUser> members(Long sessionId);

    List<ImSessionMemberUser> blockMembers(Long sessionId);

    ImSessionListResult session(Long sessionId);

    List<ImSessionApply> pendingApplies(Long userId);

    List<ImSessionApply> sentApplies(Long userId);

    void alasSessionMember(SessionAliasParam param);

    void blockSessionMember(PeerSessionBlockParam param);

    void unblockSessionMember(PeerSessionBlockParam param);

    void exit(PeerSessionExitParam param);

    void publishGroupAnnouncement(GroupAnnouncementParam param);

    ImSessionAnnouncement announcement(Long sessionId, Long userId);
}
