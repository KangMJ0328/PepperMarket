package com.example.demo.service;

import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Service
public class MemberService {
    
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    
    @Autowired
    private HttpSession session;

    @Autowired
    public MemberService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
	    this.userService = userService;
    }

    // 사용자 이메일로 사용자 찾기
    public Users findByEmail(String email) throws Exception {
        Optional<Users> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new Exception("User not found");
        }
    }

    // 프로필 정보 업데이트
    public void updateUserProfileInfo(String username, String nickname, String email, String name, Date birthdate) throws Exception {
        Users user = findByEmail(username);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setName(name);
        user.setBirthdate(birthdate);
        userRepository.save(user);
    }
    public void updateProfilePic(String username, String profilePicPath) throws Exception {
        Users user = findByEmail(username);
        user.setProfilePicPath(profilePicPath);
        userRepository.save(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PrincipalDetails principalDetails) {
            // 사용자 정보가 업데이트된 경우 세션에 반영
            if (principalDetails.getUsername().equals(user.getUsername())) {
                principalDetails.getUser().setProfilePicPath(profilePicPath);
            }
        }
    }
    public void updateNickname(String username, String nickname) throws Exception {
        Users user = findByEmail(username);
        user.setNickname(nickname);
        userRepository.save(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PrincipalDetails principalDetails) {
	        // 사용자 정보가 업데이트된 경우 세션에 반영
            if (principalDetails.getUsername().equals(user.getUsername())) {
                principalDetails.getUser().setNickname(nickname);
            }
        }
    }
    

    public void updateName(String username, String name) throws Exception {
        Users user = findByEmail(username);
        user.setName(name);
        userRepository.save(user);
    }

    public void updateBirthdate(String username, Date birthdate) throws Exception {
        Users user = findByEmail(username);
        user.setBirthdate(birthdate);
        userRepository.save(user);
    }
    
    public void changePassword(String username, String currentPassword, String newPassword) throws Exception {
        Users user = findByEmail(username);
        if (user.getProvider() != null) {
            throw new Exception("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
