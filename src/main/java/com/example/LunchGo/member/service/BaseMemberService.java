package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.member.domain.CustomRole;
import com.example.LunchGo.member.domain.UserStatus;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Log4j2
public class BaseMemberService implements MemberService {
    private final UserRepository userRepository;

    @Override
    public void save(UserJoinRequest userReq) {

        if(userRepository.existsByEmail(userReq.getEmail())) {
            log.info("User already exists with email: " + userReq.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }
        userRepository.save(User.builder()
                .email(userReq.getEmail())
                .password(userReq.getPassword()) //암호화 필수
                .name(userReq.getName())
                        .companyName(userReq.getCompanyName())
                        .companyAddress(userReq.getCompanyAddress())
                        .phone(userReq.getPhone())
                        .marketingAgree(userReq.getMarketingAgree())
                        .role(CustomRole.USER)
                        .emailAuthentication(false) //기본값
                        .status(UserStatus.ACTIVE) //기본값
                .build());
    }
}
