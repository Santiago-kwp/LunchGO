package com.example.LunchGo.email.controller;

import com.example.LunchGo.email.dto.EmailDTO;
import com.example.LunchGo.email.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/email/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailDTO emailDTO) {
        if(!StringUtils.hasLength(emailDTO.getMail())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //필수 요소
        try {
            emailService.sendEmail(emailDTO.getMail());
        }catch(MessagingException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); //메일이 안보내질 경우 500 에러 처리
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Boolean> verifyEmail(@RequestBody EmailDTO emailDTO) {
        if(!StringUtils.hasLength(emailDTO.getMail()) || !StringUtils.hasLength(emailDTO.getVerifyCode())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        boolean isVerify = emailService.verifyEmailCode(emailDTO.getMail(), emailDTO.getVerifyCode());
        return ResponseEntity.ok(isVerify);
    }
}
