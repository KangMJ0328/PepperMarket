package com.example.demo.service;

import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
