package com.example.demo.controller;

import com.example.demo.entity.Board;
import com.example.demo.entity.Report;
import com.example.demo.service.ReportService;
import com.example.demo.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private BoardService boardService;

    @PostMapping
    public String report(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam Long postId,
                         @RequestParam String reason,
                         RedirectAttributes redirectAttributes) {
        String reporterEmail = userDetails.getUsername();
        Board board = boardService.getBoardById(postId);
        Long reportedUserId = board.getUser().getId();
        reportService.createReport(reporterEmail, reportedUserId, reason, postId);
        redirectAttributes.addFlashAttribute("message", "신고가 성공적으로 접수되었습니다.");
        return "redirect:/board/view?id=" + postId;
    }

    @GetMapping("/admin/reports")
    public String getReports(Model model) {
        List<Report> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        return "admin/user-list";
    }
}
