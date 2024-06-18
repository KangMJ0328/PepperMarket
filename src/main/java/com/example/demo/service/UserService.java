package com.example.demo.service;

import com.example.demo.dto.AddUserRequest;
import com.example.demo.entity.UserRole;
import com.example.demo.entity.Users;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor // Lombok을 사용하여 필요한 생성자를 자동으로 생성
@Service // 서비스 클래스임을 나타냄
public class UserService {

    @Autowired
    private final UserRepository userRepository; // 사용자 레포지토리
    private final BoardRepository boardRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 암호화를 위한 인코더

    // 새로운 사용자를 생성하고 저장하는 메서드
    public Long save(AddUserRequest dto) {
        if ("admin".equalsIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Cannot use 'admin' as nickname");
        }
        // 사용자 정보를 생성하고 저장
        String email = dto.getEmail();
        String nickname = email.substring(0, email.indexOf("@"));
        Users newUser = Users.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(dto.getPassword())) // 비밀번호를 암호화하여 저장
                .name(dto.getName()) // 닉네임 추가
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
        return userRepository.save(newUser).getId(); // 사용자의 ID 반환
    }

    // 사용자 ID로 사용자를 조회하는 메서드
    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    // 사용자 이메일로 사용자를 조회하는 메서드
    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<Users> getUserProfile(Long id) {
        return userRepository.findById(id);
    }

    public void suspendUser(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        user.setSuspended(true);
        userRepository.save(user);
    }

    public void unsuspendUser(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        user.setSuspended(false);
        userRepository.save(user);
    }

}
