package com.example.demo.repository;

import com.example.demo.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporter(String reporter);

    // *** 추가: 특정 신고된 사용자를 찾는 메소드 추가 ***
    List<Report> findByReportedUser(String reportedUser);
}
