package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.FindPwdRequest;
import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.member.dto.MemberInfo;
import com.example.LunchGo.member.dto.MemberUpdateInfo;
import com.example.LunchGo.member.dto.OwnerInfo;
import com.example.LunchGo.member.dto.OwnerUpdateInfo;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.User;

public interface MemberService {
    void save(UserJoinRequest userReq);

    void existsByEmail(String email);

    void save(OwnerJoinRequest ownerReq);

    void existsByLoginId(String loginId);

    User find(String name, String phone);

    Owner find(String name, String businessNum, String phone);

    void check(FindPwdRequest findPwdReq);

    void updatePwd(FindPwdRequest findPwdReq);

    MemberInfo getMemberInfo(Long userId);

    void updateMemberInfo(Long userId, MemberUpdateInfo memberUpdateInfo);

    OwnerInfo getOwnerInfo(Long ownerId);

    void updateOwnerInfo(Long ownerId, OwnerUpdateInfo ownerUpdateInfo);
}
