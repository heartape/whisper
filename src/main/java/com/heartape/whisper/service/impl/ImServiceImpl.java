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
import org.owasp.encoder.Encode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    private final SimpMessagingTemplate messagingTemplate;


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

        ImSessionApply imSessionApply = imSessionApplyMapper.selectByApplicantIdAndReviewerId(applicantId, reviewerId, ApplyTypeEnum.FRIEND);
        if (imSessionApply == null) {
            ImSessionApply apply = new ImSessionApply();
            apply.setType(ApplyTypeEnum.FRIEND);
            apply.setApplicantId(applicantId);
            apply.setReviewerId(reviewerId);
            apply.setAliasName(param.getAliasName());
            apply.setStatus(ApplyStatusEnum.PENDING);
            apply.setApplyInfo(param.getApplyInfo());
            imSessionApplyMapper.insert(apply);
        } else {
            if (imSessionApply.getStatus() == ApplyStatusEnum.REJECTED) {
                throw new BusinessException("已被拒绝,不可再次申请");
            } else if (imSessionApply.getStatus() == ApplyStatusEnum.APPROVED) {
                /* ############################ **/
                Long uidMin = Math.min(applicantId, reviewerId);
                Long uidMax = Math.max(applicantId, reviewerId);
                ImPeerSession peerSession = imPeerSessionMapper.selectByUsers(uidMin, uidMax);
                if (peerSession == null) {
                    // 目前对于ImSessionApply是重复利用的，所以ImSessionApply与ImPeerSession是同时为null或同时不为null
                    throw new BusinessException("会话数据异常");
                }
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
                /* ############################ **/
                if (!reviewerSessionMember.getIsExit()) {
                    throw new BusinessException("已添加好友,不可再次申请");
                }
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
        switch (apply.getType()) {
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

        createSessionMember(session.getId(), userId, GroupRoleEnum.OWNER, null);
        return session.getId();
    }

    @Override
    public void applyGroupSession(GroupSessionApplyParam groupSessionApplyParam) {
        final Long sessionId = groupSessionApplyParam.getSessionId();
        final Long userId = groupSessionApplyParam.getUserId();

        ImSession session = imSessionMapper.selectById(sessionId);
        if (session == null || session.getType() != SessionEnum.GROUP) {
            throw new BusinessException("群会话不存在");
        }
        final ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, null, null);
        if (imSessionMember != null) {
            if (imSessionMember.getIsBlock()) {
                throw new BusinessException("已被拉黑");
            }
            if (!imSessionMember.getIsExit()) {
                throw new BusinessException("已加入群聊,不可再次申请");
            }
        }
        ImSessionApply imSessionApply = imSessionApplyMapper.pendingGroupApply(sessionId, userId);
        Long ownerId = resolveGroupOwnerId(sessionId);
        if (imSessionApply == null) {
            ImSessionApply apply = new ImSessionApply();
            apply.setType(ApplyTypeEnum.GROUP);
            apply.setSessionId(sessionId);
            apply.setApplicantId(userId);
            apply.setReviewerId(ownerId);
            apply.setStatus(ApplyStatusEnum.PENDING);
            apply.setApplyInfo(groupSessionApplyParam.getApplyInfo());
            imSessionApplyMapper.insert(apply);
        } else {
            final ApplyStatusEnum status = imSessionApply.getStatus();
            switch (status) {
                case PENDING -> throw new BusinessException("申请已存在,请勿重复申请");
                case APPROVED -> {
                }
                case REJECTED -> throw new BusinessException("申请已被拒绝,不可再次申请");
            }
            imSessionApply.setReviewerId(ownerId);
            imSessionApply.setStatus(ApplyStatusEnum.PENDING);
            imSessionApply.setApplyInfo(groupSessionApplyParam.getApplyInfo());
            imSessionApplyMapper.updateForReset(imSessionApply);
        }
    }

    @Override
    public void managePeerMember(PeerMemberManageParam param) {
        final Long operatorId = param.getOperatorId();
        final Long userId = param.getUserId();
        if (operatorId.equals(userId)) {
            throw new BusinessException("不可以对自己进行" + param.getAction() + "操作");
        }
        final ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(param.getSessionId(), operatorId, null, null, null);
        if (imSessionMember == null || imSessionMember.getIsExit()) {
            throw new BusinessException("操作人不在会话中");
        }
        if (imSessionMember.getIsBlock()) {
            throw new BusinessException("操作人在黑名单");
        }

        final ImSessionMember value;
        final ImSessionMember condition;
        switch (param.getAction()) {
            case BLOCK -> {
                value = new ImSessionMember(null, null, null, null, null, null, null, true, null, null);
                condition = new ImSessionMember(null, param.getSessionId(), userId, null, null, null, null, false, null, null);
            }
            case UNBLOCK -> {
                value = new ImSessionMember(null, null, null, null, null, null, null, false, null, null);
                condition = new ImSessionMember(null, param.getSessionId(), userId, null, null, null, null, true, null, null);
            }
            default -> throw new BusinessException("未知操作");
        }
        imSessionMemberMapper.updateBySessionIdAndUserId(value, condition);
    }

    @Override
    public void manageGroupMember(GroupMemberManageParam param) {
        final Long sessionId = param.getSessionId();
        final Long operatorId = param.getOperatorId();
        final Long userId = param.getUserId();
        final GroupMemberActionEnum action = param.getAction();
        if (operatorId.equals(userId)) {
            throw new BusinessException("不可以对自己进行" + action + "操作");
        }

        GroupRoleEnum operatorRole = imSessionMemberMapper.selectRoleBySessionIdAndUserId(sessionId, operatorId);
        if (operatorRole == null) {
            throw new BusinessException("操作人不在群聊中");
        }

        GroupRoleEnum userRole = imSessionMemberMapper.selectRoleBySessionIdAndUserId(sessionId, userId);
        if (userRole == null) {
            throw new BusinessException("用户不在群聊中");
        }
        if (GroupRoleEnum.OWNER.equals(userRole)) {
            throw new BusinessException("群主不可操作");
        }

        final boolean isNotAdminOrOwner = !GroupRoleEnum.OWNER.equals(operatorRole) && !GroupRoleEnum.ADMIN.equals(operatorRole);
        final boolean isPairAdmin = GroupRoleEnum.ADMIN.equals(operatorRole) && GroupRoleEnum.ADMIN.equals(userRole);

        ImSessionMember condition = new ImSessionMember(null, sessionId, userId, null, null, null, null, false, null, null);
        ImSessionMember value = new ImSessionMember(null, sessionId, userId, null, null, null, null, false, null, null);
        switch (action) {
            case SET_ADMIN -> {
                if (!GroupRoleEnum.OWNER.equals(operatorRole)) {
                    throw new BusinessException("仅群主可设置管理员");
                }
                // imSessionMemberMapper.updateRole(sessionId, userId, GroupRoleEnum.ADMIN.name());
                condition.setRole(GroupRoleEnum.MEMBER);
                value.setRole(GroupRoleEnum.ADMIN);
            }
            case REMOVE_ADMIN -> {
                if (!GroupRoleEnum.OWNER.equals(operatorRole)) {
                    throw new BusinessException("仅群主可移除管理员");
                }
                // imSessionMemberMapper.updateRole(sessionId, userId, GroupRoleEnum.MEMBER.name());
                condition.setRole(GroupRoleEnum.ADMIN);
                value.setRole(GroupRoleEnum.MEMBER);
            }
            // 管理员被提出后role修改为user
            case KICK -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可踢出成员");
                }
                if (isPairAdmin) {
                    throw new BusinessException("管理员不可踢出管理员");
                }
                // imSessionMemberMapper.updateKickBySessionIdAndUserId(sessionId, userId);
                condition.setIsExit(false);
                value.setIsExit(true);
            }
            case BLOCK_AND_KICK -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可拉黑成员");
                }
                if (isPairAdmin) {
                    throw new BusinessException("管理员不可拉黑管理员");
                }
                // imSessionMemberMapper.updateBlockAndKickBySessionIdAndUserId(sessionId, userId);
                condition.setIsExit(false);
                value.setIsExit(true);
                condition.setIsBlock(false);
                value.setIsBlock(true);
            }
            case UNBLOCK -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可解除拉黑成员");
                }
                // imSessionMemberMapper.updateBlockBySessionIdAndUserId(sessionId, userId, false);
                condition.setIsBlock(true);
                value.setIsBlock(false);
            }
            case MUTE -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可禁言成员");
                }
                if (isPairAdmin) {
                    throw new BusinessException("管理员不可禁言管理员");
                }
                // imSessionMemberMapper.updateMuteBySessionIdAndUserId(sessionId, userId, true);
                condition.setIsMute(false);
                value.setIsMute(true);
            }
            case UNMUTE -> {
                if (isNotAdminOrOwner) {
                    throw new BusinessException("仅管理员可解除禁言成员");
                }
                // imSessionMemberMapper.updateMuteBySessionIdAndUserId(sessionId, userId, false);
                condition.setIsMute(true);
                value.setIsMute(false);
            }
            default -> throw new BusinessException("不支持的操作");
        }
        imSessionMemberMapper.updateBySessionIdAndUserId(condition, value);
    }

    @Override
    public List<ImSessionResult> sessions(Long userId) {
        return imSessionMapper.selectListByUserId(userId);
    }

    @Override
    public List<UserContactsResult> contacts(Long userId) {
        return imSessionMemberMapper.selectUserListByUserId(userId);
    }

    @Override
    public List<ImSessionSearchResult> sessions(SessionEnum type, String keyword) {
        List<ImSession> sessions = switch (type) {
            case GROUP -> imSessionMapper.selectByNameMatch(type, keyword);
            case PEER, SYSTEM -> throw new BusinessException("未知会话类型");
        };

        return sessions
                .stream()
                .map(ImSessionSearchResult::of)
                .collect(Collectors.toList());

    }

    @Override
    public List<ImMessageResult> messages(Integer count, Long since, Long before) {
        return imSessionMapper.messages(count, since, before)
                .stream()
                .map(ImMessageResult::of)
                .toList();
    }

    @Override
    public List<ImMessageResult> messages(Long sessionId, Integer count, Long since, Long before) {
        return imSessionMapper.messages(sessionId, count, since, before)
                .stream()
                .map(ImMessageResult::of)
                .toList();
    }

    @Override
    public List<ImMessageResult> syncMessages(Long userId, Long sessionId, MessageSyncParam messageSyncParam) {
        if (!imSessionMapper.check(sessionId, userId, null)) {
            throw new BusinessException("无权限同步会话消息");
        }
        int limit = messageSyncParam.getLimit() == null ? 50 : Math.min(Math.max(messageSyncParam.getLimit(), 1), 200);
        return imSessionMapper.syncMessages(sessionId, messageSyncParam.getBeforeMessageId(), limit)
                .stream()
                .map(ImMessageResult::of)
                .toList();
    }

    @Override
    @Transactional
    public void send(ImMessageParam messageParam) {
        Long userId = messageParam.getUserId();
        Long sessionId = messageParam.getSessionId();
        ImSessionMember senderMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, false, false);
        if (senderMember == null) {
            throw new BusinessException("发送失败，你不在会话中");
        }

        ImMessage imMessage = messageParam.toImMessage();
        imMessage.setCreateTime(System.currentTimeMillis());
        imMessageMapper.insert(imMessage);
        send(imMessage);
    }

    private void send(ImMessage imMessage) {
        final ImMessageResult imMessageResult = ImMessageResult.of(imMessage);
        String messageInfo = imMessageResult.getMessageInfo();
        String safeMessageInfo = Encode.forHtml(messageInfo);
        imMessage.setMessageInfo(safeMessageInfo);
        imSessionMemberMapper.selectListBySessionIdAndExitAndBlock(imMessage.getSessionId(), null, false, false)
                .stream()
                .map(ImSessionMemberUser::getUserId)
                .forEach(id -> messagingTemplate.convertAndSendToUser(id.toString(), "/queue/session", imMessageResult));
    }

    @Override
    public List<ImSessionMemberUser> members(Long sessionId) {
        return imSessionMemberMapper.selectListBySessionIdAndExitAndBlock(sessionId, null, false, null);
    }

    @Override
    public ImSessionMemberUser member(Long sessionId, Long userId) {
        imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, false, null);
        return null;
    }

    @Override
    public ImSessionResult session(Long sessionId) {
        ImSession imSession = imSessionMapper.selectById(sessionId);
        Long userId = authenticationContext.getUserId();
        ImSessionMember imSessionMember = imSessionMemberMapper.selectBySessionIdAndUserId(sessionId, userId, null, false, false);
        if (imSessionMember == null) {
            throw new RuntimeException("用户不在会话中");
        }

        return ImSessionResult.of(imSession);
    }

    @Override
    public List<ImSessionApplyResult> pendingApplies(Long userId) {
        return imSessionApplyMapper.selectListByReviewerId(userId)
                .stream()
                .map(ImSessionApplyResult::of)
                .toList();

    }

    @Override
    public List<ImSessionApplyResult> sentApplies(Long userId) {
        return imSessionApplyMapper.selectListByApplicantId(userId)
                .stream()
                .map(ImSessionApplyResult::of)
                .toList();
    }

    @Override
    public ImSessionMemberUser alasSessionMember(SessionAliasParam param) {
        final Long sessionId = param.getSessionId();

        ImSessionMember value = new ImSessionMember(null, null, null, null, param.getAliasName(), null, null, null, null, null);
        ImSessionMember condition = new ImSessionMember(null, sessionId, param.getUserId(), null, null, null, null, null, null, null);

        final ImSession imSession = imSessionMapper.selectById(sessionId);
        if (imSession == null) {
            throw new BusinessException("会话不存在");
        }

        switch (imSession.getType()) {
            case PEER -> {
                ImPeerSession peerSession = imPeerSessionMapper.selectBySessionId(sessionId);
                if (peerSession == null) {
                    throw new BusinessException("会话不存在");
                }
                Long userId;
                if (peerSession.getUid1().equals(param.getUserId())) {
                    userId = peerSession.getUid2();
                } else if (peerSession.getUid2().equals(param.getUserId())) {
                    userId = peerSession.getUid1();
                } else {
                    throw new BusinessException("用户不在会话中");
                }
                condition.setUserId(userId);
                imSessionMemberMapper.updateBySessionIdAndUserId(value, condition);
                return imSessionMemberMapper.selectUserBySessionIdAndUserId(sessionId, condition.getUserId(), null, null, null);
            }
            case GROUP -> {
                imSessionMemberMapper.updateBySessionIdAndUserId(value, condition);
                return imSessionMemberMapper.selectUserBySessionIdAndUserId(sessionId, condition.getUserId(), null, null, null);
            }
            case SYSTEM -> throw new BusinessException("系统会话暂不支持");
            default -> throw new IllegalStateException("Unexpected value: " + imSession.getType());
        }
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
    public ImSessionAnnouncementResult announcement(Long sessionId, Long userId) {
        boolean exists = imSessionMemberMapper.existsBySessionIdAndUserId(sessionId, userId, null, false, false);
        if (!exists) {
            throw new BusinessException("用户不在会话中");
        }
        final ImSessionAnnouncement imSessionAnnouncement = imSessionAnnouncementMapper.selectBySessionId(sessionId);
        return ImSessionAnnouncementResult.of(imSessionAnnouncement);
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

        createSessionMember(sessionId, applicantId, GroupRoleEnum.NULL, applicantAliasName);
        createSessionMember(sessionId, reviewerId, GroupRoleEnum.NULL, reviewerAliasName);
        // 进群后添加打招呼消息
        final ImMessage joinSessionMessage = createSessionMessage(sessionId, applicantId, MessageEnum.TEXT, messageInfo);
        send(joinSessionMessage);
    }

    private ImMessage createSessionMessage(Long sessionId, Long userId, MessageEnum messageType, String messageInfo) {
        final ImMessage imMessage = new ImMessage();
        imMessage.setSessionId(sessionId);
        imMessage.setUserId(userId);
        imMessage.setMessageType(messageType);
        imMessage.setMessageInfo(messageInfo);
        imMessage.setCreateTime(System.currentTimeMillis());
        return imMessage;
    }

    private void joinGroupSession(Long sessionId, Long userId, String username, String messageInfo) {
        createSessionMember(sessionId, userId, GroupRoleEnum.MEMBER, null);
        // 进群后添加打招呼消息
        final ImMessage joinSessionMessage = createSessionMessage(sessionId, userId, MessageEnum.TEXT, messageInfo);
        send(joinSessionMessage);
        // 进群后添加系统消息
        final ImMessage systemSessionMessage = createSessionMessage(sessionId, 0L, MessageEnum.SYSTEM_JOIN_GROUP, username);
        send(systemSessionMessage);
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
        member.setJoinTime(System.currentTimeMillis());
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
