package com.example.demo.controller;

import com.example.demo.entity.Board;
import com.example.demo.entity.PrincipalDetails;
import com.example.demo.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller // Spring MVC에서 컨트롤러로 작동하도록 지정
public class MainController {
    
    @Autowired // BoardService를 주입받아 사용
    private BoardService boardService;
    
    // 루트 URL ("/") 요청을 처리하는 메서드
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @PageableDefault(page = 0, size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        if (principalDetails != null) {
            model.addAttribute("user", principalDetails.getUsers());
        }
        // 조회수가 높은 상위 10개의 게시글을 가져옴
        List<Board> topPosts = boardService.getTop10PostsByViewcount();
        // 모델에 topPosts라는 이름으로 게시글 리스트를 추가
        model.addAttribute("topPosts", topPosts);
        
        
        Page<Board> list = boardService.getAvailableBoard8();
        model.addAttribute("list", list);
        
        // "main" 뷰를 반환 (resources/templates/main.html 파일을 가리킴)
        return "main"; // main.html 템플릿 반환
    }


    // "/chat" URL 요청을 처리하는 메서드
    @GetMapping("/chat")
    public ModelAndView showChatMainPage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        ModelAndView modelAndView = new ModelAndView("chatRoom");
        modelAndView.addObject("userId", principalDetails.getId()); // 수신자 ID를 모델에 추가
        return modelAndView;
    }
    
    @GetMapping("/noti")
    public String noti() {
        return "noti";
    }
    
    @GetMapping("/usernametest")
    public String usernametest() {
        return "usernametest";
    }
}
