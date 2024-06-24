package com.example.demo.repository;

import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface UserRepository extends JpaRepository<Users, Long> {
    // email로 사용자 정보 가져옴
    Optional<Users> findByEmail(String email);

    Optional<Users> findByNickname(String nickname); // nickname으로 사용자 정보 가져옴

    @Query("SELECT u FROM Users u")
    List<Users> findAllUsers();
    
    Optional<Users> findUserIdByEmail(String email);

    Optional<Users> findEmailById(Long userId);
    
    
}
