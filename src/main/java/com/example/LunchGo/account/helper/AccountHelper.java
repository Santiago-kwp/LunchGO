package com.example.LunchGo.account.helper;

import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;

public interface AccountHelper {

    void userJoin(UserJoinRequest userReq);

    void checkEmail(String email);

    void ownerJoin(OwnerJoinRequest userReq);

    void checkLoginId(String loginId);
}
