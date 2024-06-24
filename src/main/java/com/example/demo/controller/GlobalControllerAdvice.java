package com.example.demo.controller;

import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.Users;
import com.example.demo.service.BoardService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 프로젝트 전역적으로 적용되는 컨트롤러 어드바이스입니다.
// 모든 컨트롤러에 대해 공통 작업을 수행할 수 있습니다.
import java.util.List;
import java.util.Map;


// 프로젝트 전역적으로 적용되는 컨트롤러입니다.


@ControllerAdvice
public class GlobalControllerAdvice {
    
    private final NotificationService notificationService;
    // 모든 요청에 대해 공통적으로 실행될 메서드입니다.
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BoardService boardService;
    
    @Autowired
    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    
    @ModelAttribute
    public void addAttributes(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 사용자 닉네임을 저장할 변수 초기화
        String nick = "";
        // 사용자 프로필 사진 URL을 저장할 변수 초기화
        String profilepic = "";
        Long loggedUserId = 0L;



        try {
            // PrincipalDetails 객체에서 사용자 프로필 사진 URL을 가져옵니다.
            profilepic = principalDetails.getProfilePic();
        } catch (Exception e) {
            // 예외 발생 시 기본 프로필 사진 URL을 설정합니다.
            profilepic = "https://cdn-icons-png.flaticon.com/512/3135/3135715.png";
        } finally {
            // 모델에 프로필 사진 URL을 추가합니다.
            model.addAttribute("profilepic", profilepic);
        }

        try{
            loggedUserId = principalDetails.getId();
        } catch(Exception e){
            loggedUserId = Long.valueOf("0");
        }finally {
            model.addAttribute("loggedUserId", loggedUserId);

        }

        List<String> categList = categoryService.getCategoryList();
        model.addAttribute("categList", categList);
        
        int unreadNotificationCount = 0;
        
        try {
            unreadNotificationCount = notificationService.getUnreadNotificationCount(loggedUserId);
        } catch (Exception e) {
            unreadNotificationCount = 0;
        } finally {
            model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        }
        
        
    }
    @ModelAttribute("myProfilePic")
    public String globalProfilePicPath(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        String profilepic = "";
        try {
            // PrincipalDetails 객체에서 사용자 프로필 사진 URL을 가져옵니다.
            profilepic = principalDetails.getProfilePic();
        } catch (Exception e) {
            // 예외 발생 시 기본 프로필 사진 URL을 설정합니다.
            profilepic = "https://cdn-icons-png.flaticon.com/512/3135/3135715.png";
        } finally {
            // 모델에 프로필 사진 URL을 추가합니다.
            model.addAttribute("profilepic", profilepic);
        }
        return profilepic;
    }
    
    
}
