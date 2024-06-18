// src/main/java/com/example/demo/repository/ViewedPostRepository.java
package com.example.demo.repository;

import com.example.demo.entity.ViewedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewedPostRepository extends JpaRepository<ViewedPost, Long> {
    List<ViewedPost> findByUserIdOrderByViewedAtDesc(Long userId);
}
