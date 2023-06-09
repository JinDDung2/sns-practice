package com.example.fasns.application.controller;

import com.example.fasns.domain.member.dto.*;
import com.example.fasns.domain.member.service.MemberLoginService;
import com.example.fasns.domain.member.service.MemberReadService;
import com.example.fasns.domain.member.service.MemberWriteService;
import com.example.fasns.global.security.MemberDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Slf4j
public class MemberController {

    private final MemberWriteService memberWriteService;
    private final MemberReadService memberReadService;
    private final MemberLoginService memberLoginService;

    @PostMapping("/register")
    public Response<MemberDto> register(@RequestBody MemberRegisterDto registerDto) {
        return Response.success(memberWriteService.register(registerDto), CREATED);
    }

    @PostMapping("/login")
    public Response<TokenInfo> login(@RequestBody MemberLoginDto loginDto) {
        return Response.success(memberLoginService.login(loginDto), OK);
    }


    @PostMapping("/reissue")
    public Response<TokenDto> reissue(@RequestBody TokenDto tokenDto) {
        return Response.success(memberLoginService.reissue(tokenDto), OK);
    }

    @PostMapping("/logout")
    public Response<Void> logout(@RequestBody TokenDto tokenDto) {
        memberLoginService.logout(tokenDto);
        return Response.success(OK);
    }

    @GetMapping("")
    public Response<MemberDto> getMember(@AuthenticationPrincipal MemberDetail member) {
        return Response.success(memberReadService.getMember(member.getUsername()), OK);
    }

    @GetMapping("/nickname-histories")
    public Response<List<MemberNicknameHistoryDto>> getNicknameHistories(@AuthenticationPrincipal MemberDetail member) {
        return Response.success(memberReadService.getNicknameHistories(member.getUsername()), OK);
    }

    @PostMapping("/change/nickname")
    public Response<MemberDto> changeNickname(@AuthenticationPrincipal MemberDetail member, @RequestBody String nickname) {
        memberWriteService.changeNickname(member.getUsername(), nickname);
        return Response.success(memberReadService.getMember(member.getUsername()), OK);
    }

    @PostMapping("/change/password")
    Response<Void> changePassword(@AuthenticationPrincipal MemberDetail member, @RequestBody String password) {
        memberWriteService.changePassword(member.getUsername(), password);
        return Response.success(OK);
    }

    @PostMapping("/change/role")
    Response<Void> changeRole(@AuthenticationPrincipal MemberDetail admin, Long memberId) {
        memberWriteService.upgradeMemberRole(admin.getMember(), memberId);
        return Response.success(OK);
    }
    @DeleteMapping("")
    public Response<Void> delete(@AuthenticationPrincipal MemberDetail member) {
        memberWriteService.delete(member.getUsername());
        return Response.success(OK);
    }
}
