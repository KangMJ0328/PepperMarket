// src/main/java/com/example/demo/service/ViewedPostService.java
package com.example.demo.service;

import com.example.demo.entity.ViewedPost;
import com.example.demo.repository.ViewedPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViewedPostService {
    @Autowired
    private ViewedPostRepository viewedPostRepository;

    public void addViewedPost(Long userId, Long postId) {
        ViewedPost viewedPost = new ViewedPost();
        viewedPost.setUserId(userId);
        viewedPost.setPostId(postId);
        viewedPost.setViewedAt(LocalDateTime.now());
        viewedPostRepository.save(viewedPost);
    }

    public List<ViewedPost> getRecentViewedPosts(Long userId) {
        return viewedPostRepository.findByUserIdOrderByViewedAtDesc(userId);
    }
}
