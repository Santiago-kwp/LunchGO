package com.example.LunchGo.account.controller;

import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.account.helper.AccountHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api")
public class AccountController {
    private final AccountHelper accountHelper;

    @PostMapping("/join/user")
    public ResponseEntity<?> userJoin(@RequestBody UserJoinRequest userReq) {
        if(!StringUtils.hasLength(userReq.getEmail()) || !StringUtils.hasLength(userReq.getPassword()) || !StringUtils.hasLength(userReq.getName()) ||
           !StringUtils.hasLength(userReq.getPhone()) || !StringUtils.hasLength(userReq.getCompanyName()) || !StringUtils.hasLength(userReq.getCompanyAddress())) {
            //이메일 수신 동의는 필수 아님
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountHelper.userJoin(userReq);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/email")
    public ResponseEntity<?> checkEmail(@RequestBody UserJoinRequest userReq) {
        if(!StringUtils.hasLength(userReq.getEmail())) { //이메일 검증이니 이메일만 체크
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountHelper.checkEmail(userReq.getEmail()); //존재하면 여기서 409 에러 던짐
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/join/owner")
    public ResponseEntity<?> ownerJoin(@RequestBody OwnerJoinRequest ownerReq) {
        if(!StringUtils.hasLength(ownerReq.getLoginId()) || !StringUtils.hasLength(ownerReq.getPassword()) || !StringUtils.hasLength(ownerReq.getName()) ||
            !StringUtils.hasLength(ownerReq.getPhone()) || !StringUtils.hasLength(ownerReq.getBusinessNum())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        accountHelper.ownerJoin(ownerReq);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/loginId")
    public ResponseEntity<?> checkLoginId(@RequestBody OwnerJoinRequest ownerReq) {
        if(!StringUtils.hasLength(ownerReq.getLoginId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        accountHelper.checkLoginId(ownerReq.getLoginId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
