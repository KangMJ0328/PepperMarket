package com.example.demo.repository;

import com.example.demo.entity.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBoardId(Integer boardId);
    Optional<Comment> findById(Integer commentId);
    @Transactional
    void deleteByBoardId(Integer boardId);

}