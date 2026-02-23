package com.heartape.whisper.service.impl;

import com.heartape.whisper.common.*;
import com.heartape.whisper.common.constant.*;
import com.heartape.whisper.entity.*;
import com.heartape.whisper.entity.Param.*;
import com.heartape.whisper.entity.result.*;
import com.heartape.whisper.exception.BusinessException;
import com.heartape.whisper.mapper.*;
import com.heartape.whisper.service.ImService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ImServiceImpl implements ImService {

    private final ImSessionMapper imSessionMapper;

    private final ImSessionApplyMapper imSessionApplyMapper;

    private final ImSessionMemberMapper imSessionMemberMapper;

    private final ImMessageMapper imMessageMapper;

    private final AuthenticationContext authenticationContext;

    private final ImPeerSessionMapper imPeerSessionMapper;

    private final UserMapper userMapper;

    private final ImSessionAnnouncementMapper imSessionAnnouncementMapper;

    /**
     * todo:重复检查
     */
    @Override
    public void applyPeerSession(PeerSessionApplyParam param) {
        Long applicantId = param.getApplicantId();
        final Long reviewerId = param.getReviewerId();
        if (applicantId.equals(reviewerId)) {
            throw new BusinessException("不能添加自己为好友");
        }
        final User user = userMapper.findById(reviewerId);
        if (user== null) {
            throw new BusinessException("用户不存在");
        }

        Long uidMin = Math.min(applicantId, reviewerId);
        Long uidMax = Math.max(applicantId, reviewerId);
        ImPeerSession peerSession = imPeerSessionMapper.selectByUsers(uidMin, uidMax);
        if (peerSession != null) {
            final Long sessionId = peerSession.getSessionId();
            final List<ImSessionMember> imSessionMemberList = imSessionMemberMapper.selectListBySessionIdAndPairUserId(sessionId, reviewerId, applicantId);

            final Map<Long, ImSessionMember> imSessionMemberMap = imSessionMemberList
                    .stream()
                    .collect(Collectors.toMap(ImSessionMember::getUserId, imSessionMember -> imSessionMember));
            final ImSessionMember applicantSessionMember = imSessionMemberMap.get(applicantId);
            final ImSessionMember reviewerSessionMember = imSessionMemberMap.get(reviewerId);
            if (applicantSessionMember == null || reviewerSessionMember == null) {
                throw new BusinessException("会话:" + sessionId + " 数据异常");
            }
            if (applicantSessionMember.getIsBlock() || reviewerSessionMember.getIsBlock()) {
                throw new BusinessException("已被拉黑");
            }
        }

        ImSessionApply imSessionApply = imSessionApplyMapper.selectByApplicantIdAndReviewerId(applicantId, reviewerId, ApplyBizTypeEnum.FRIEND);
        if (imSessionApply == null) {
            ImSessionApply apply = new ImSessionApply();
            apply.setBizType(ApplyBizTypeEnum.FRIEND);
            apply.setApplicantId(applicantId);
            apply.setReviewerId(reviewerId);
            apply.setAliasName(param.getAliasName());
            apply.setStatus(ApplyStatusEnum.PENDING);
            apply.setApplyInfo(param.getApplyInfo());
            imSessionApplyMapper.insert(apply);
        } else {
            if (imSessionApply.getStatus() == ApplyStatusEnum.REJECTED) {
                throw new BusinessException("已被拒绝,不可再次申请");
            }
            // 重置ImSessionApply
            imSessionApply.setAliasName(param.getAliasName());
            imSessionApply.setStatus(ApplyStatusEnum.PENDING);
            imSessionApply.setApplyInfo(param.getApplyInfo());
            imSessionApplyMapper.updateForReset(imSessionApply);
        }
    }

    @Override
    @Transactional
    public void reviewApply(Long userId, ApplyReviewParam applyReviewParam) {
        ImSessionApply apply = imSessionApplyMapper.selectById(applyReviewParam.getApplyId());
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        if (apply.getStatus() != ApplyStatusEnum.PENDING) {
            throw new BusinessException("申请已处理,勿重复操作");
        }
        final Long reviewerId = apply.getReviewerId();
        if (!userId.equals(reviewerId)) {
            throw new BusinessException("无权限处理该申请");
        }

        ApplyStatusEnum status = Boolean.TRUE.equals(applyReviewParam.getApproved()) ? ApplyStatusEnum.APPROVED : ApplyStatusEnum.REJECTED;
        int changed = imSessionApplyMapper.updateReview(apply.getId(), status, userId, applyReviewParam.getReviewNote());
        if (changed == 0) {
            throw new BusinessException("申请状态更新失败");
        }

        if (status == ApplyStatusEnum.REJECTED) {
            return;
        }

        final Long applicantId = apply.getApplicantId();
        switch (apply.getBizType()) {
            case FRIEND -> joinPeerSession(applicantId, reviewerId, applyReviewParam.getAliasName(), apply.getAliasName(), apply.getApplyInfo());
            case GROUP -> {
                final User user = userMapper.selectUsernameById(applicantId);
                joinGroupSession(apply.getSessionId(), applicantId, user.getUsername(), apply.getApplyInfo());
            }
            default -> throw new BusinessException("未知申请类型");
        }
    }

    @Override
    @Transactional
    public Long createGroupSession(GroupSessionParam groupSessionParam) {
        final Long userId = groupSessionParam.getUserId();
        final User user = userMapper.selectUsernameById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        ImSession session = new ImSession();
        session.setType(SessionEnum.GROUP);
        session.setName(groupSessionParam.getName());
        session.setIcon(groupSessionParam.getIcon());
        imSessionMapper.insert(session);

        createSessionMember(session.getId(), userId, GroupRoleEnum.OWNER);
        return session.getId();
    }

    /**
     * todo:重复检查
     */
    @Override
    public void applyGroupSession(GroupSessionApplyParam groupSessionApplyParam) {
        final Long sessionId = groupSessionApplyParam.getSessionId();
        final Long userId = groupSessionApplyParam.getUserId();

        ImSession session = imSessionMapper.selectById(sessionId);
        if (session == null || session.getType() != SessionEnum.GROUP) {
            throw new BusinessException("群会话不存在");
        }
        if (imSessionMemberMapper.exists(sessionId, userId)) {
            throw new BusinessException("已在群聊中");
        }
        ImSessionApply imSessionApply = imSessionApplyMapper.pendingGroupApply(sessionId, userId);
        Long ownerId = resolveGroupOwnerId(sessionId);
        if (imSessionApply == null) {
            ImSessionApply apply = new ImSessionApply();
            apply.setBizType(ApplyBizTypeEnum.GROUP);
            apply.setSessionId(sessionId);
            apply.setApplicantId(userId);
            apply.setReviewerId(ownerId);
            apply.setStatus(ApplyStatusEnum.PENDING);
            apply.setApplyInfo(groupSessionApplyParam.getApplyInfo());
            imSessionApplyMapper.insert(apply);
        } else {
            imSessionApply.setReviewerId(ownerId);
            imSessionApply.setStatus(ApplyStatusEnum.PENDING);
            imSessionApply.setApplyInfo(groupSessionApplyParam.getApplyInfo());
            imSessionApplyMapper.updateForReset(imSessionApply);
        }
    }

    @Override
    public void manageGroupMember(GroupMemberManageParam param) {
        final Long operatorId = param.getOperatorId();
        final Long userId = param.getUserId();
        if (operatorId.equals(userId)) {
            throw new BusinessException("不可以对自己进行GroupMemberManageParam操作");
        }

        GroupRoleEnum operatorRole = imSessionMemberMapper.selectRoleBySessionIdAndUserId(param.getSessionId(), operatorId);
        if (operatorRole == null) {
            throw new BusinessException("操作人不在群聊中");
        }

        GroupRoleEnum userRole = imSessionMemberMapper.selectRoleBySessionIdAndUserId(param.getSessionId(), userId);
        if (userRole == null) {
            throw new BusinessException("用户不在群聊中");
        }
        if (GroupRoleEnum.OWNER.equals(userRole)) {
            throw new BusinessException("群主不可操作");
        }

        final boolean isNotAdminOrOwner = !GroupRoleEnum.OWNER.equals(operatorRole) && !GroupRoleEnum.ADMIN.equals(operatorRole);
        final boolean isPairAdmin = GroupRoleEnum.ADMIN.equals(operatorRole) && GroupRoleEnum.ADMIN.equals(userRole);
        switch (param.getAction()) {
            case SET_ADMIN -> {
                if (!GroupRoleEnum.OWNER.equals(operatorRole)) {
                    throw new BusinessException("仅群主可设置管理员");
                }
                imSessionMemberMapper.updateRole(param.getSessionId(), userId, GroupRoleEnum.ADMIN.name());
            }
            case REMOVE_ADMIN -> {
                if (!GroupRoleEnum.OWNER.equals(operatorRole)) {
                    throw new BusinessException("仅群主可移除管理员");
                }
                imSessionMemberMapper.updateRole(param.getSessionId(), userId, GroupRoleEnum.MEMBER.name());
            }
            // 管理员被提出后role修改为user
            case KICK -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可踢出成员");
                }
                if (isPairAdmin) {
                    throw new BusinessException("管理员不可踢出管理员");
                }
                imSessionMemberMapper.updateKickBySessionIdAndUserId(param.getSessionId(), userId);
            }
            case BLOCK_AND_KICK -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可拉黑成员");
                }
                if (isPairAdmin) {
                    throw new BusinessException("管理员不可拉黑管理员");
                }
                imSessionMemberMapper.updateBlockAndKickBySessionIdAndUserId(param.getSessionId(), userId);
            }
            case UNBLOCK -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可解除拉黑成员");
                }
                imSessionMemberMapper.updateBlockBySessionIdAndUserId(param.getSessionId(), userId, false);
            }
            case MUTE -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可禁言成员");
                }
                if (isPairAdmin) {
                    throw new BusinessException("管理员不可禁言管理员");
                }
                imSessionMemberMapper.updateMuteBySessionIdAndUserId(param.getSessionId(), userId, true);
            }
            case UNMUTE -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可解除禁言成员");
                }
                imSessionMemberMapper.updateMuteBySessionIdAndUserId(param.getSessionId(), userId, false);
            }
            default -> throw new BusinessException("不支持的操作");
        }
    }

    @Override
    public List<ImSessionResult> sessions(Long userId) {
        final List<ImSession> imSessions = imSessionMapper.selectListByUserId(userId);
        final List<ImSessionWithMemberUser> imSessionWithMemberUsers = imSessionMapper.selectPeerListWithUserByNotUserId(userId);
        final Map<Long, ImSessionWithMemberUser> peerSessionMap = imSessionWithMemberUsers
                .stream()
                .collect(Collectors.toMap(ImSessionWithMemberUser::getId, Function.identity()));
        return imSessions
                .stream()
                .peek(session -> {
                    if (SessionEnum.PEER.equals(session.getType())) {
                        ImSessionWithMemberUser peerSession = peerSessionMap.get(session.getId());
                        if (peerSession != null) {
                            session.setIcon(peerSession.getIcon());
                            session.setName(peerSession.getName());
                        }
                    }
                }).map(ImSessionResult::of)
                .toList();
    }

    @Override
    public List<ImSessionSearchResult> sessions(SessionEnum sessionEnum, String keyword) {
        List<ImSession> sessions = switch (sessionEnum) {
            case PEER, GROUP -> imSessionMapper.selectByNameMatch(sessionEnum, keyword);
            case SYSTEM -> throw new BusinessException("未知会话类型");
        };

        return sessions
                .stream()
                .map(ImSessionSearchResult::of)
                .collect(Collectors.toList());

    }

    @Override
    public List<ImMessage> messages(Long sessionId) {
        return imSessionMapper.messages(sessionId);
    }

    @Override
    public List<ImMessage> syncMessages(Long userId, Long sessionId, MessageSyncParam messageSyncParam) {
        if (!imSessionMapper.check(sessionId, userId, null)) {
            throw new BusinessException("无权限同步会话消息");
        }
        int limit = messageSyncParam.getLimit() == null ? 50 : Math.min(Math.max(messageSyncParam.getLimit(), 1), 200);
        return imSessionMapper.syncMessages(sessionId, messageSyncParam.getBeforeMessageId(), limit);
    }

    @Override
    @Transactional
    public ImMessageResult send(ImMessageParam messageParam) {
        Long userId = messageParam.getUserId();
        Long sessionId = messageParam.getSessionId();
        ImSessionMember senderMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, false, false);
        if (senderMember == null) {
            throw new BusinessException("发送失败，你不在会话中");
        }

        ImMessage imMessage = messageParam.toImMessage();
        imMessageMapper.insert(imMessage);
        imSessionMapper.touchUpdateTime(sessionId);
        return ImMessageResult.of(imMessage);
    }

    @Override
    public List<ImSessionMemberUser> members(Long sessionId) {
        return imSessionMemberMapper.selectListBySessionIdAndExitAndBlock(sessionId, false, null);
    }

    @Override
    public List<ImSessionMemberUser> blockMembers(Long sessionId) {
        return imSessionMemberMapper.selectListBySessionIdAndExitAndBlock(sessionId, null, true);
    }

    @Override
    public ImSessionListResult session(Long sessionId) {
        ImSession imSession = imSessionMapper.selectById(sessionId);
        Long userId = authenticationContext.getUserId();
        ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, false, null);
        if (imSessionMember == null) {
            throw new RuntimeException("用户不在会话中");
        }

        imSessionMemberMapper.resetUnread(sessionId, userId);

        switch (imSession.getType()) {
            case PEER -> {
                return imSessionMapper.peerSession(sessionId, userId);
            }
            case GROUP -> {
                return imSessionMapper.groupSession(sessionId);
            }
            case SYSTEM -> throw new RuntimeException("系统会话暂不支持");
            default -> throw new RuntimeException("会话类型错误");
        }
    }

    @Override
    public List<ImSessionApply> pendingApplies(Long userId) {
        return imSessionApplyMapper.selectListByReviewerId(userId);
    }

    @Override
    public List<ImSessionApply> sentApplies(Long userId) {
        return imSessionApplyMapper.selectListByApplicantId(userId);
    }

    @Override
    public void alasSessionMember(SessionAliasParam param) {
        final Long sessionId = param.getSessionId();
        final Long userId = param.getUserId();

        ImSessionMember value = new ImSessionMember(null, null, null, null, param.getAliasName(), null, null, null, null, null);
        ImSessionMember condition = new ImSessionMember(null, sessionId, userId, null, null, null, null, null, null, null);

        updateSessionMember(sessionId, userId, value, condition);
    }

    private void updateSessionMember(Long sessionId, Long userId, ImSessionMember value, ImSessionMember condition) {
        ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, null, null);
        if (imSessionMember == null) {
            throw new BusinessException("用户不在会话中");
        }

        final ImSession imSession = imSessionMapper.selectById(sessionId);
        if (imSession == null) {
            throw new BusinessException("会话不存在");
        }
        int update = switch (imSession.getType()) {
            case PEER ->
                    imSessionMemberMapper.updateBySessionIdAndNotUserId(value, condition);
            case GROUP ->
                    imSessionMemberMapper.updateBySessionIdAndUserId(value, condition);
            case SYSTEM ->
                    throw new BusinessException("系统会话暂不支持");
        };
        if (update == 0) {
            throw new BusinessException("修改失败");
        }
    }

    @Override
    public void blockSessionMember(PeerSessionBlockParam param) {
        final Long sessionId = param.getSessionId();
        final Long userId = param.getUserId();

        final ImSessionMember value = new ImSessionMember(null, null, null, null, null, null, null, true, null, null);
        final ImSessionMember condition = new ImSessionMember(null, sessionId, userId, null, null, null, null, false, null, null);

        updateSessionMember(sessionId, userId, value, condition);
    }

    @Override
    public void unblockSessionMember(PeerSessionBlockParam param) {
        final Long sessionId = param.getSessionId();
        final Long operatorId = param.getOperatorId();
        final ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, operatorId, null, false, false);
        if (imSessionMember == null) {
            throw new BusinessException("操作者不在会话中");
        }
        final GroupRoleEnum role = imSessionMember.getRole();
        if (role != GroupRoleEnum.ADMIN && role != GroupRoleEnum.OWNER) {
            throw new BusinessException("权限不足");
        }

        final Long userId = param.getUserId();
        final ImSessionMember value = new ImSessionMember(null, null, null, null, null, null, null, false, null, null);
        final ImSessionMember condition = new ImSessionMember(null, sessionId, userId, null, null, null, null, true, null, null);

        updateSessionMember(sessionId, userId, value, condition);
    }

    @Override
    public void exit(PeerSessionExitParam param) {
        final Long sessionId = param.getSessionId();
        final Long userId = param.getUserId();
        final int delete = imSessionMemberMapper.deleteBySessionIdAndUserId(sessionId, userId);
        if (delete == 0) {
            throw new BusinessException("退出失败");
        }
    }

    @Override
    public void publishGroupAnnouncement(GroupAnnouncementParam param) {
        final Long sessionId = param.getSessionId();
        final Long userId = param.getUserId();
        final ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, false, false);
        if (imSessionMember == null) {
            throw new BusinessException("用户不在会话中");
        }
        final GroupRoleEnum role = imSessionMember.getRole();
        if (role != GroupRoleEnum.ADMIN && role != GroupRoleEnum.OWNER) {
            throw new BusinessException("权限不足");
        }
        final ImSessionAnnouncement imSessionAnnouncement = new ImSessionAnnouncement(null, sessionId, userId, param.getContent(), null);
        imSessionAnnouncementMapper.insertOrUpdate(imSessionAnnouncement);
    }

    @Override
    public ImSessionAnnouncement announcement(Long sessionId, Long userId) {
        boolean exists = imSessionMemberMapper.existsBySessionIdAndUserId(sessionId, userId, null, false, false);
        if (!exists) {
            throw new BusinessException("用户不在会话中");
        }
        return imSessionAnnouncementMapper.selectBySessionId(sessionId);
    }

    /**
     * 加入私聊会话
     * @param applicantId applicantId
     * @param reviewerId reviewerId
     * @param applicantAliasName applicant别名，由reviewer设置
     * @param reviewerAliasName reviewer别名，由applicant设置
     * @param messageInfo 打招呼消息
     */
    private void joinPeerSession(Long applicantId, Long reviewerId,
                                 String applicantAliasName, String reviewerAliasName,
                                 String messageInfo) {
        Long uid1 = Math.min(applicantId, reviewerId);
        Long uid2 = Math.max(applicantId, reviewerId);
        ImPeerSession existsSession = imPeerSessionMapper.selectByUsers(uid1, uid2);
        Long sessionId;
        if (existsSession == null) {
            ImSession session = new ImSession();
            session.setType(SessionEnum.PEER);
            imSessionMapper.insert(session);

            sessionId = session.getId();
            ImPeerSession peerSession = new ImPeerSession();
            peerSession.setUid1(uid1);
            peerSession.setUid2(uid2);
            peerSession.setSessionId(sessionId);
            peerSession.setApplyStatus(PeerApplyEnum.PERMIT);
            imPeerSessionMapper.insert(peerSession);
        } else {
            sessionId = existsSession.getSessionId();
        }

        createSessionMember(sessionId, applicantId, applicantAliasName);
        createSessionMember(sessionId, reviewerId, reviewerAliasName);
        createJoinSessionMessage(sessionId, applicantId, messageInfo);
    }

    /**
     * 进群后添加打招呼消息
     */
    private void createJoinSessionMessage(Long sessionId, Long userId,String messageInfo) {
        createSessionMessage(sessionId, userId, MessageEnum.TEXT, messageInfo);
    }

    /**
     * 系统消息
     */
    private void createSystemSessionMessage(Long sessionId, MessageEnum messageType, String messageInfo) {
        createSessionMessage(sessionId, 0L, messageType, messageInfo);
    }

    private void createSessionMessage(Long sessionId, Long userId, MessageEnum messageType, String messageInfo) {
        final ImMessage imMessage = new ImMessage();
        imMessage.setSessionId(sessionId);
        imMessage.setUserId(userId);
        imMessage.setMessageType(messageType);
        imMessage.setMessageInfo(messageInfo);
        imMessageMapper.insert(imMessage);
    }

    private void joinGroupSession(Long sessionId, Long userId, String username, String messageInfo) {
        createSessionMember(sessionId, userId, GroupRoleEnum.MEMBER);
        createJoinSessionMessage(sessionId, userId, messageInfo);
        // 进群后添加系统消息
        createSystemSessionMessage(sessionId, MessageEnum.SYSTEM_JOIN_GROUP, username);
    }

    /**
     * 私聊专用
     */
    private void createSessionMember(Long sessionId, Long userId, String aliasName) {
        createSessionMember(sessionId, userId, GroupRoleEnum.NULL, aliasName);
    }

    /**
     * 群聊专用
     */
    private void createSessionMember(Long sessionId, Long userId, GroupRoleEnum role) {
        createSessionMember(sessionId, userId, role, null);
    }

    private void createSessionMember(Long sessionId, Long userId, GroupRoleEnum role, String aliasName) {
        ImSessionMember member = new ImSessionMember();
        member.setSessionId(sessionId);
        member.setUserId(userId);
        member.setAliasName(aliasName);
        member.setRole(role);
        member.setIsMute(false);
        member.setIsExit(false);
        member.setIsBlock(false);
        imSessionMemberMapper.insertOrUpdate(member);
    }

    private Long resolveGroupOwnerId(Long sessionId) {
        List<Long> members = imSessionMemberMapper.userIds(sessionId);
        List<Long> owners = new ArrayList<>();
        for (Long memberId : members) {
            GroupRoleEnum role = imSessionMemberMapper.selectRoleBySessionIdAndUserId(sessionId, memberId);
            if (GroupRoleEnum.OWNER.equals(role)) {
                owners.add(memberId);
            }
        }
        return owners.stream().min(Comparator.naturalOrder()).orElseThrow(() -> new BusinessException("群主不存在"));
    }

}
