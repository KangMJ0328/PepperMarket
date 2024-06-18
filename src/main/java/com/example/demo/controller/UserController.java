package com.example.demo.controller;

import com.example.demo.dto.AddUserRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.Users;
import com.example.demo.service.BoardService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final BoardService boardService;

    @PostMapping("/user")
    public String signup(@Valid AddUserRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            bindingResult.rejectValue("passwordCheck", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup";
        }

        try {
            userService.save(request);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(AddUserRequest addUserRequest) {
        return "signup";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/";
    }

    @GetMapping("/profile/{id}")
    public String getUserProfile(@PathVariable Long id, Model model, PrincipalDetails principalDetails, Pageable pageable) {
        Optional<Users> userProfile = userService.getUserProfile(id);
        Page<Board> list = boardService.getBoardByUserId(id, pageable);

        Long userPostCount = boardService.getBoardCountByUserId(id);

        model.addAttribute("user", userProfile.orElse(null));
        model.addAttribute("userPostCount", userPostCount);
        model.addAttribute("principal", principalDetails);
        model.addAttribute("list", list);

        return "userProfile";
    }
}
