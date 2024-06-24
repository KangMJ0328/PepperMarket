package com.example.demo.service;

import com.example.demo.dto.AddUserRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.UserRole;
import com.example.demo.entity.Users;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor // Lombok을 사용하여 필요한 생성자를 자동으로 생성
@Service // 서비스 클래스임을 나타냄
public class UserService {
    @Autowired
    private final UserRepository userRepository; // 사용자 레포지토리
    private final BoardRepository boardRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 암호화를 위한 인코더

    // 새로운 사용자를 생성하고 저장하는 메서드
    public Long save(AddUserRequest dto, MultipartFile file) {
        if ("admin".equalsIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Cannot use 'admin' as nickname");
        }
        // 사용자 정보를 생성하고 저장
        String email = dto.getEmail();
        String nickname = email.substring(0, email.indexOf("@"));
        String profilePicPath = null;

        if (file != null && !file.isEmpty()) {
            try {
                // 파일 저장 로직 (예: 파일 시스템, 클라우드 스토리지 등)
                profilePicPath = saveProfilePicture(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile picture", e);
            }
        } else if(file == null || file.isEmpty()) {
            profilePicPath = "/files/profile/defaultProfile.png";
        }

        Users newUser = Users.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(dto.getPassword())) // 비밀번호를 암호화하여 저장
                .name(dto.getName()) // 닉네임 추가
                .nickname(nickname)
                .role(UserRole.USER)
                .profilePicPath(profilePicPath) // 프로필 사진 경로 추가
                .build();
        return userRepository.save(newUser).getId(); // 사용자의 ID 반환
    }

    // 프로필 사진 저장 로직 구현
    public String saveProfilePicture(MultipartFile profilePicture) throws IOException {
        // UUID를 사용하여 파일명 생성
        String fileName = UUID.randomUUID() + "_" + profilePicture.getOriginalFilename();
        // 프로파일 사진 저장 경로 설정 (서버 환경에 맞게)
        String profilePath = "/home/ec2-user/pepper/files/profile";
        Path directoryPath = Paths.get(profilePath);
        // 디렉토리가 없으면 생성
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        Path filePath = directoryPath.resolve(fileName);
        // 파일 복사 및 저장
        Files.copy(profilePicture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/files/profile/" + fileName;
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

    @Transactional
    public void updateUserProfileImage(Long userId, String imagePath) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfilePicPath(imagePath);
        userRepository.save(user);
    }
}
