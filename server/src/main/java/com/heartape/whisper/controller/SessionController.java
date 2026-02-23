package com.heartape.whisper.controller;

import com.heartape.whisper.common.Result;
import com.heartape.whisper.common.constant.SessionEnum;
import com.heartape.whisper.common.ThreadLocalAuthenticationContext;
import com.heartape.whisper.entity.ImMessage;
import com.heartape.whisper.entity.ImSessionAnnouncement;
import com.heartape.whisper.entity.ImSessionApply;
import com.heartape.whisper.entity.ImSessionMemberUser;
import com.heartape.whisper.entity.Param.*;
import com.heartape.whisper.entity.result.ImSessionListResult;
import com.heartape.whisper.entity.result.ImSessionResult;
import com.heartape.whisper.entity.result.ImSessionSearchResult;
import com.heartape.whisper.service.ImService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/find")
    public Result<?> sessions(@RequestParam SessionEnum sessionEnum, @RequestParam String keyword) {
        List<ImSessionSearchResult> sessions = imService.sessions(sessionEnum, keyword);
        return Result.success(sessions);
    }

    @GetMapping("/{id}")
    public Result<?> session(@PathVariable Long id) {
        ImSessionListResult imSession = imService.session(id);
        return Result.success(imSession);
    }

    @GetMapping("/{id}/messages")
    public Result<?> messages(@PathVariable Long id) {
        List<ImMessage> messages = imService.messages(id);
        return Result.success(messages);
    }

    @GetMapping("/{id}/members")
    public Result<?> members(@PathVariable Long id) {
        final List<ImSessionMemberUser> sessionMembers = imService.members(id);
        final List<ImSessionMemberUser> blockMembers = imService.blockMembers(id);
        return Result.success(Map.of("member", sessionMembers, "block", blockMembers));
    }

    @PostMapping("/{id}/messages/sync")
    public Result<?> syncMessages(@PathVariable Long id, @RequestBody MessageSyncParam messageSyncParam) {
        Long userId = authenticationContext.getUserId();
        List<ImMessage> messages = imService.syncMessages(userId, id, messageSyncParam);
        return Result.success(messages);
    }

    @PutMapping("/alias")
    public Result<?> alias(@RequestBody SessionAliasParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        imService.alasSessionMember(param);
        return Result.success();
    }

    @PutMapping("/block")
    public Result<?> block(@RequestBody PeerSessionBlockParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        imService.blockSessionMember(param);
        return Result.success();
    }

    @PutMapping("/unblock")
    public Result<?> unblock(@RequestBody PeerSessionBlockParam param) {
        Long userId = authenticationContext.getUserId();
        param.setOperatorId(userId);
        imService.unblockSessionMember(param);
        return Result.success();
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
        ImSessionAnnouncement imSessionAnnouncement = imService.announcement(id, userId);
        return Result.success(imSessionAnnouncement);
    }

    @PostMapping("/announcement")
    public Result<?> announcement(@RequestBody GroupAnnouncementParam param) {
        Long userId = authenticationContext.getUserId();
        param.setUserId(userId);
        imService.publishGroupAnnouncement(param);
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
        List<ImSessionApply> applies = imService.pendingApplies(userId);
        return Result.success(applies);
    }

    @GetMapping("/apply/sent")
    public Result<?> sentApplies() {
        Long userId = authenticationContext.getUserId();
        List<ImSessionApply> applies = imService.sentApplies(userId);
        return Result.success(applies);
    }

    @PostMapping("/apply/review")
    public Result<?> reviewApply(@RequestBody ApplyReviewParam param) {
        Long userId = authenticationContext.getUserId();
        imService.reviewApply(userId, param);
        return Result.success();
    }

}
