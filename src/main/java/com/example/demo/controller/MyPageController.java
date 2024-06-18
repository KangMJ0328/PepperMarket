package com.example.demo.controller;

import com.example.demo.entity.Report;
import com.example.demo.entity.Users;
import com.example.demo.service.MemberService;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@Controller
public class MyPageController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) throws Exception {
        String email = userDetails.getUsername();
        Users user = memberService.findByEmail(email);
        model.addAttribute("user", user);
        return "mypage";
    }

    @PostMapping("/mypage/change-profile-info")
    public String changeProfileInfo(@RequestParam("nickname") String nickname,
                                    @RequestParam("email") String email,
                                    @RequestParam("name") String name,
                                    @RequestParam("birthdate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthdate,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            memberService.updateUserProfileInfo(userDetails.getUsername(), nickname, email, name, birthdate);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "An error occurred: " + e.getMessage());
        }

        return "redirect:/mypage";
    }

    @GetMapping("/mypage/reports")
    public String myReports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        try {
            Users user = memberService.findByEmail(email);
            List<Report> myReports = reportService.getReportsByReporter(user.getEmail());
            model.addAttribute("myReports", myReports);
            return "mypage/reports"; // mypage/reports.html 뷰를 반환합니다.
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while fetching the data.");
            return "redirect:/error"; // 에러 발생 시 에러 페이지로 리디렉션합니다.
        }
    }

    @PostMapping("/mypage/report")
    public String createReport(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("reportedUser") String reportedUser,
                               @RequestParam("reason") String reason,
                               RedirectAttributes redirectAttributes) {
        String email = userDetails.getUsername();
        try {
            Users user = memberService.findByEmail(email);
            Report report = Report.builder()
                    .reporter(user.getEmail())
                    .reportedUser(reportedUser)
                    .reason(reason)
                    .reportedAt(new Date())
                    .build();
            reportService.createReport(report);
            redirectAttributes.addFlashAttribute("message", "Report created successfully.");
            return "redirect:/mypage/reports"; // 신고 목록 페이지로 리디렉션합니다.
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while creating the report.");
            return "redirect:/error"; // 에러 발생 시 에러 페이지로 리디렉션합니다.
        }
    }
}
