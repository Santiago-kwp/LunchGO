package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이미 존재하는 이메일인지 확인
     */
    boolean existsByEmail(String email);

    /**
     * name과 phone으로 사용자 email 찾기
     * */
    Optional<User> findByNameAndPhone(String name, String phone);

    /**
     * email, name, phone으로 사용자 존재 확인(비밀번호 찾기에서 사용)
     * */
    boolean existsByEmailAndNameAndPhone(String email, String name, String phone);

    /**
     * 인증된 email로 비밀번호 변경
     * */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int updatePassword(@Param("email") String email, @Param("password") String password);
}
