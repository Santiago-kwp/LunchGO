package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    /**
     * 이미 존재하는 아이디인지 확인
     * */
    boolean existsByLoginId(String loginId);

    /**
     * name, businessNum, phone으로 아이디 찾기
     * */
    Optional<Owner> findByNameAndBusinessNumAndPhone(String name, String businessNum, String phone);

    /**
     * loginId, name, phone으로 사업자 존재 확인
     * */
    boolean existsByLoginIdAndNameAndPhone(String loginId, String name, String phone);

    /**
     * 인증된 loginId로 비밀번호 변경
     * */
    @Modifying(clearAutomatically = true) //영속성 컨텍스트 초기화
    @Query("UPDATE Owner o SET o.password = :password WHERE o.loginId = :loginId")
    int updatePassword(@Param("loginId") String loginId, @Param("password") String password);
}
