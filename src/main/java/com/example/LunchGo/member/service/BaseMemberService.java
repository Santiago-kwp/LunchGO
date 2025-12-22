package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.member.domain.CustomRole;
import com.example.LunchGo.member.domain.OwnerStatus;
import com.example.LunchGo.member.domain.UserStatus;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.OwnerRepository;
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
    private final OwnerRepository ownerRepository;

    @Override
    public void save(UserJoinRequest userReq) {
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

    @Override
    public void existsByEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            log.info("User already exists with email: " + email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }
    }

    @Override
    public void save(OwnerJoinRequest ownerReq) {
        ownerRepository.save(Owner.builder()
                .loginId(ownerReq.getLoginId())
                .password(ownerReq.getPassword())
                .name(ownerReq.getName())
                .startAt(ownerReq.getStartAt())
                .businessNum(ownerReq.getBusinessNum())
                .phone(ownerReq.getPhone())
                .role(CustomRole.OWNER)
                .status(OwnerStatus.PENDING)
                .build());
    }

    @Override
    public void existsByLoginId(String loginId) {
        if(ownerRepository.existsByLoginId(loginId)) {
            log.info("User already exists with loginId: " + loginId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }
    }
}
