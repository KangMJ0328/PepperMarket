package com.example.demo.service;

import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor // Lombok을 사용하여 생성자를 자동으로 생성
@Service // 서비스 클래스임을 나타냄
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository; // 사용자 레포지토리

    // 주어진 이메일로 사용자를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사용자를 찾고, 없으면 예외 발생
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다.")); // 사용자를 찾지 못하면 예외 발생

        // 찾은 사용자를 PrincipalDetails 객체로 변환하여 반환
        return new PrincipalDetails(user);
    }
}
