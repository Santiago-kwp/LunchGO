package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    /**
     * 이미 존재하는 아이디인지 확인
     * */
    boolean existsByLoginId(String loginId);
}
