package com.example.demo.repository;

import com.example.demo.entity.Keyword;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByUser(Users user);
    List<Keyword> findByUserOrderByKeywordAsc(Users user);
    Optional<Keyword> findByUserAndKeyword(Users user, String keyword);
}
