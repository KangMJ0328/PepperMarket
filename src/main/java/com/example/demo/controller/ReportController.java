package com.example.demo.controller;

import com.example.demo.dto.ReportRequest;
import com.example.demo.entity.Report;
import com.example.demo.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @PostMapping
    public ResponseEntity<String> reportPost(ReportRequest reportRequest, RedirectAttributes redirectAttributes) {
        Report report = Report.builder()
                .reporter("anonymous@example.com") // 신고자 이메일 - 실제 구현에서는 인증된 사용자 이메일을 사용
                .reportedUser("reported@example.com") // 신고된 사용자 이메일 - 실제 구현에서는 게시글 작성자 이메일을 사용
                .reason(reportRequest.getReason())
                .reportedAt(new Date())
                .build();
        reportRepository.save(report);
        redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다.");
        return ResponseEntity.ok("신고가 접수되었습니다.");
    }
}
