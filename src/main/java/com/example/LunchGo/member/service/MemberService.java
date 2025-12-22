package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;

public interface MemberService {
    void save(UserJoinRequest userReq);

    void existsByEmail(String email);

    void save(OwnerJoinRequest ownerReq);

    void existsByLoginId(String loginId);
}
