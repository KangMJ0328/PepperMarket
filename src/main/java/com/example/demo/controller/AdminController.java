package com.example.demo.controller;

import com.example.demo.entity.UserRole;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user-list")
    public String userList(@AuthenticationPrincipal UserDetails userDetails, Model model) throws Exception {
        logger.info("Attempting to access /admin/user-list");

        if (userDetails != null) {
            Users currentUser = memberService.findByEmail(userDetails.getUsername());
            if (currentUser != null && UserRole.ADMIN.equals(currentUser.getRole())) {
                List<Users> users = userRepository.findAll();
                model.addAttribute("users", users);
                logger.info("User List - Access granted for admin");
                return "admin/user-list";
            } else {
                logger.warn("Access Denied: User is not an admin");
                return "redirect:/";
            }
        }
        logger.warn("Access Denied: User is not authenticated");
        return "redirect:/login";
    }

    @PostMapping("/suspend/{userId}")
    public String suspendUser(@PathVariable Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
        user.setSuspended(true);
        userRepository.save(user);
        return "redirect:/admin/user-list";
    }

    @PostMapping("/unsuspend/{userId}")
    public String unsuspendUser(@PathVariable Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
        user.setSuspended(false);
        userRepository.save(user);
        return "redirect:/admin/user-list";
    }
}
