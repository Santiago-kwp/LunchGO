package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이미 존재하는 이메일인지 확인
     */
    boolean existsByEmail(String email);
}
