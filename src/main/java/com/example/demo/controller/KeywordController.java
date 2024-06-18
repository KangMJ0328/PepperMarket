package com.example.demo.controller;

import com.example.demo.entity.Keyword;
import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.Users;
import com.example.demo.dto.KeywordRequest;
import com.example.demo.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/keywords") // 키워드 관련 API 요청을 처리하는 컨트롤러입니다.
public class KeywordController {

    @Autowired
    private KeywordService keywordService; // 키워드 관련 서비스

    // 키워드를 추가하는 엔드포인트
    @PostMapping
    public ResponseEntity<Keyword> addKeyword(@RequestBody KeywordRequest keywordRequest) {
        // 현재 인증된 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Users user = principalDetails.getUsers(); // 사용자 정보를 가져옴

        // 새로운 키워드 객체를 생성하고 사용자와 키워드 정보를 설정
        Keyword keyword = new Keyword();
        keyword.setKeyword(keywordRequest.getKeyword());
        keyword.setUser(user);
        
        // 키워드를 저장하고 HTTP 응답으로 반환
        try {
            return ResponseEntity.ok(keywordService.save(keyword));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 중복된 키워드인 경우 409 Conflict 응답
        }
    }

    // 키워드를 삭제하는 엔드포인트
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long id) {
        keywordService.delete(id); // 키워드 삭제
        return ResponseEntity.noContent().build(); // HTTP 204 No Content 응답 반환
    }

    // 현재 사용자의 키워드 리스트를 가져오는 엔드포인트
    @GetMapping
    public ResponseEntity<List<Keyword>> getKeywords() {
        // 현재 인증된 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Users user = principalDetails.getUsers(); // 사용자 정보를 가져옴

        // 사용자의 키워드 리스트를 검색하고 HTTP 응답으로 반환
        return ResponseEntity.ok(keywordService.findByUser(user));
    }
}
