package com.example.demo.repository;

import com.example.demo.entity.Board;
import com.example.demo.entity.Report;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportedPost(Board reportedPost);
    void deleteByReportedPostId(Integer postId);

    List<Report> findByReportedUser(Users reportedUser);
}
