package com.example.LunchGo.account.helper;

import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Log4j2
public class AccountHelperImpl implements AccountHelper {

    private final MemberService memberService;

    @Override
    public void userJoin(UserJoinRequest userReq) {
        memberService.save(userReq); //회원가입
    }
}
