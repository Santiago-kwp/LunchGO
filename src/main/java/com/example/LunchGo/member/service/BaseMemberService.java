package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.FindPwdRequest;
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
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
        if (userRepository.existsByEmail(email)) {
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
        if (ownerRepository.existsByLoginId(loginId)) {
            log.info("User already exists with loginId: " + loginId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }
    }

    @Override
    public User find(String name, String phone) {
        return userRepository.findByNameAndPhone(name, phone).orElseThrow( () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 이메일을 가진 사용자가 없습니다."
                ));

    }

    @Override
    public Owner find(String name, String businessNum, String phone) {
        return ownerRepository.findByNameAndBusinessNumAndPhone(name, businessNum, phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 아이디를 가진 사업자가 없습니다."));
    }

    @Override
    public void check(FindPwdRequest findPwdReq) {
        if(StringUtils.hasLength(findPwdReq.getEmail())) { //사용자인 경우
            if(!userRepository.existsByEmailAndNameAndPhone(findPwdReq.getEmail(), findPwdReq.getName(), findPwdReq.getPhone())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }
            return;
        }
        if(StringUtils.hasLength(findPwdReq.getLoginId())) { //사업자인 경우
            if(!ownerRepository.existsByLoginIdAndNameAndPhone(findPwdReq.getLoginId(), findPwdReq.getName(), findPwdReq.getPhone())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다.");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST); //아이디나 이메일을 입력하지 않은 경우
    }

    @Override
    @Transactional
    public void updatePwd(FindPwdRequest findPwdReq) { //비밀번호 암호화 필수
        if(StringUtils.hasLength(findPwdReq.getEmail())) { //사용자인 경우
            if(userRepository.updatePassword(findPwdReq.getEmail(), findPwdReq.getPassword()) == 0){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }
            return;
        }
        if(StringUtils.hasLength(findPwdReq.getLoginId())) { //사업자인 경우
            if(ownerRepository.updatePassword(findPwdReq.getLoginId(), findPwdReq.getPassword()) == 0){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다.");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST); //아이디나 이메일이 없는 경우
    }
}