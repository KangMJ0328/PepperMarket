package com.example.demo.repository;

import com.example.demo.entity.Like;
import com.example.demo.entity.Board;
import com.example.demo.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBoardIdAndUserEmail(Integer boardId, String userEmail);
    long countByBoardId(Integer boardId);
    @Transactional
    void deleteByBoardId(Integer boardId);
}

