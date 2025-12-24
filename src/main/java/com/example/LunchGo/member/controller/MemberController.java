package com.example.LunchGo.member.controller;

import com.example.LunchGo.member.dto.MemberInfo;
import com.example.LunchGo.member.dto.MemberUpdateInfo;
import com.example.LunchGo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/info/user/{userId}")
    public ResponseEntity<?> userDetail(@PathVariable Long userId) {
        MemberInfo member = memberService.getMemberInfo(userId); //만약 예외 발생시 404 발생

        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @PutMapping("/info/user/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody MemberUpdateInfo memberUpdateInfo) {
        memberService.updateMemberInfo(userId, memberUpdateInfo); //예외 발생시 404 발생

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
