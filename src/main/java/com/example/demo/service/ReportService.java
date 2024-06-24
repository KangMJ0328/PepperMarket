package com.example.demo.service;

import com.example.demo.entity.Report;
import com.example.demo.entity.Users;
import com.example.demo.entity.Board;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    public void createReport(String reporterEmail, Long reportedUserId, String reason, Long postId) {
        Users reportedUser = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Users reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reporter email"));

        Board reportedPost = boardRepository.findById(Math.toIntExact(postId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));

        Report report = new Report();
        report.setReporter(reporterEmail);
        report.setReportedUser(reportedUser);
        report.setReason(reason);
        report.setReportedAt(new Date());
        report.setReporterEmail(reporterEmail);
        report.setReportedEmail(reportedUser.getEmail());
        report.setReportedPost(reportedPost);
        reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
}
