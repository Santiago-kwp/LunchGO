package com.example.LunchGo.account.helper;

import com.example.LunchGo.account.dto.*;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.User;
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

    @Override
    public void checkEmail(String email) {
        memberService.existsByEmail(email);
    }

    @Override
    public void ownerJoin(OwnerJoinRequest ownerReq) {
        memberService.save(ownerReq);
    }

    @Override
    public void checkLoginId(String loginId) {
        memberService.existsByLoginId(loginId);
    }

    @Override
    public String getEmail(UserFindRequest userReq) {
        //인증번호 전송 및 확인 로직

        User user = memberService.find(userReq.getName(), userReq.getPhone()); //없으면 404처리 완료
        return user.getEmail();
    }

    @Override
    public String getLoginId(OwnerFindRequest ownerReq) {
        //인증번호 전송 및 확인 로직

        Owner owner = memberService.find(ownerReq.getName(), ownerReq.getBusinessNum(), ownerReq.getPhone());
        return owner.getLoginId();
    }

    @Override
    public void checkMember(FindPwdRequest findPwdReq) {
        //인증번호 전송 및 확인 로직

        memberService.check(findPwdReq);
    }

    @Override
    public void changePwd(FindPwdRequest findPwdReq) {
        memberService.updatePwd(findPwdReq);
    }
}
