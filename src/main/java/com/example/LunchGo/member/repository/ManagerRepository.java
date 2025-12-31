package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    /**
     * Spring Security
     * */
    boolean existsByLoginId(String loginId);

    Optional<Manager> findByLoginId(String loginId);
}
