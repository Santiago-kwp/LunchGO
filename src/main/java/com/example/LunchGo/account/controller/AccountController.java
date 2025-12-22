package com.example.LunchGo.account.controller;

import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.account.helper.AccountHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class AccountController {
    private final AccountHelper accountHelper;

    @PostMapping("/api/join/user")
    public ResponseEntity<?> userJoin(@RequestBody UserJoinRequest userReq) {
        if(!StringUtils.hasLength(userReq.getEmail()) || !StringUtils.hasLength(userReq.getPassword()) || !StringUtils.hasLength(userReq.getName()) ||
           !StringUtils.hasLength(userReq.getPhone()) || !StringUtils.hasLength(userReq.getCompanyName()) || !StringUtils.hasLength(userReq.getCompanyAddress())) {
            //이메일 수신 동의는 필수 아님
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountHelper.userJoin(userReq);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
