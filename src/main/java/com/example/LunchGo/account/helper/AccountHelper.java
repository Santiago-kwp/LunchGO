package com.example.LunchGo.account.helper;

import com.example.LunchGo.account.dto.*;

public interface AccountHelper {

    void userJoin(UserJoinRequest userReq);

    void checkEmail(String email);

    void ownerJoin(OwnerJoinRequest userReq);

    void checkLoginId(String loginId);

    String getEmail(UserFindRequest userReq);

    String getLoginId(OwnerFindRequest ownerReq);

    void checkMember(FindPwdRequest findPwdReq);

    void changePwd(FindPwdRequest findPwdReq);
}
