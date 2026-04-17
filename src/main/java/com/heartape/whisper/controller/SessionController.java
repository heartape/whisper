package com.heartape.whisper.controller;

import com.heartape.whisper.common.Result;
import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.common.ThreadLocalAuthenticationContext;
import com.heartape.whisper.entity.ImSessionMemberUser;
import com.heartape.whisper.entity.Param.*;
import com.heartape.whisper.entity.result.*;
import com.heartape.whisper.service.ImService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/session")
public class SessionController {

    private final ImService imService;
    private final ThreadLocalAuthenticationContext authenticationContext;

    @GetMapping
    public Result<?> sessions() {
        Long userId = authenticationContext.getUserId();
        List<ImSessionResult> sessions = imService.sessions(userId);
        return Result.success(sessions);
    }

    @GetMapping("/contact")
    public Result<?> contacts() {
        Long userId = authenticationContext.getUserId();
        List<UserContactsResult> contacts = imService.contacts(userId);
        return Result.success(contacts);
    }

    @GetMapping("/find")
    public Result<?> sessions(@RequestParam SessionEnum type, @RequestParam String keyword) {
        List<ImSessionSearchResult> sessions = imService.sessions(type, keyword);
        return Result.success(sessions);
    }

    @GetMapping("/{id}")
    public Result<?> session(@PathVariable Long id) {
        ImSessionResult imSession = imService.session(id);
        return Result.success(imSession);
    }

    @GetMapping("/{id}/messages")
    public Result<?> messages(@PathVariable Long id, @RequestParam Integer count, @RequestParam(required = false) Long since, @RequestParam(required = false) Long before) {
        List<ImMessageResult> messages = imService.messages(id, count, since, before);
        return Result.success(messages);
    }

    @GetMapping("/{id}/members")
    public Result<?> members(@PathVariable Long id) {
        final List<ImSessionMemberUser> sessionMembers = imService.members(id);
        return Result.success(sessionMembers);
    }

    @GetMapping("/{sessionId}/member/{userId}")
    public Result<?> member(@PathVariable Long sessionId, @PathVariable Long userId) {
        final ImSessionMemberUser sessionMember = imService.member(sessionId, userId);
        return Result.success(sessionMember);
    }

    @PostMapping("/{id}/messages/sync")
    public Result<?> syncMessages(@PathVariable Long id, @RequestBody MessageSyncParam messageSyncParam) {
        Long userId = authenticationContext.getUserId();
        List<ImMessageResult> messages = imService.syncMessages(userId, id, messageSyncParam);
        return Result.success(messages);
    }

    @PutMapping("/alias")
    public Result<?> alias(@RequestBody SessionAliasParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        ImSessionMemberUser memberUser = imService.alasSessionMember(param);
        return Result.success(memberUser);
    }

    @DeleteMapping("/exit")
    public Result<?> exit(@RequestBody PeerSessionExitParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        imService.exit(param);
        return Result.success();
    }

    @PostMapping("/peer/apply")
    public Result<?> applyPeer(@RequestBody PeerSessionApplyParam param) {
        Long userId = authenticationContext.getUserId();
        param.setApplicantId(userId);
        imService.applyPeerSession(param);
        return Result.success();
    }

    @PostMapping("/group")
    public Result<?> createGroup(@RequestBody GroupSessionParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        final Long sessionId = imService.createGroupSession(param);
        return Result.success(sessionId);
    }

    @PostMapping("/group/apply")
    public Result<?> applyGroup(@RequestBody GroupSessionApplyParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        imService.applyGroupSession(param);
        return Result.success();
    }

    @GetMapping("/{id}/announcement")
    public Result<?> announcement(@PathVariable Long id) {
        Long userId = authenticationContext.getUserId();
        ImSessionAnnouncementResult imSessionAnnouncement = imService.announcement(id, userId);
        return Result.success(imSessionAnnouncement);
    }

    @PostMapping("/{id}/announcement")
    public Result<?> announcement(@PathVariable Long id, @RequestBody GroupAnnouncementParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        param.setSessionId(id);
        imService.publishGroupAnnouncement(param);
        return Result.success();
    }

    @PutMapping("/peer/member/manage")
    public Result<?> managePeerMember(@RequestBody PeerMemberManageParam param) {
        Long userId = authenticationContext.getUserId();
        param.setOperatorId(userId);
        imService.managePeerMember(param);
        return Result.success();
    }

    @PutMapping("/group/member/manage")
    public Result<?> manageGroupMember(@RequestBody GroupMemberManageParam param) {
        Long userId = authenticationContext.getUserId();
        param.setOperatorId(userId);
        imService.manageGroupMember(param);
        return Result.success();
    }

    @GetMapping("/apply/pending")
    public Result<?> pendingApplies() {
        Long userId = authenticationContext.getUserId();
        List<ImSessionApplyResult> applies = imService.pendingApplies(userId);
        return Result.success(applies);
    }

    @GetMapping("/apply/sent")
    public Result<?> sentApplies() {
        Long userId = authenticationContext.getUserId();
        List<ImSessionApplyResult> applies = imService.sentApplies(userId);
        return Result.success(applies);
    }

    @PostMapping("/apply/review")
    public Result<?> reviewApply(@RequestBody ApplyReviewParam param) {
        Long userId = authenticationContext.getUserId();
        imService.reviewApply(userId, param);
        return Result.success();
    }

}
