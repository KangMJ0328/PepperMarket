package com.example.demo.repository;

import com.example.demo.entity.Like;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBoardIdAndUserEmail(Integer boardId, String userEmail);

    long countByBoardId(Integer boardId);

    @Transactional
    void deleteByBoardId(Integer boardId);

    List<Like> findByUserEmail(String userEmail);
	
	
}

