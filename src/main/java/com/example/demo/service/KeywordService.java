package com.example.demo.service;

import com.example.demo.entity.Keyword;
import com.example.demo.entity.Users;
import com.example.demo.repository.KeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // 서비스 클래스임을 나타냄
public class KeywordService {

    @Autowired
    private KeywordRepository keywordRepository; // 키워드 관련 데이터 처리를 위한 리포지토리

    // 키워드를 저장하는 메서드
    public Keyword save(Keyword keyword) {
        return keywordRepository.save(keyword); // 키워드를 저장하고 반환
    }

    // 특정 ID의 키워드를 삭제하는 메서드
    public void delete(Long id) {
        keywordRepository.deleteById(id); // ID를 사용하여 키워드 삭제
    }

    // 모든 키워드를 조회하는 메서드
    public List<Keyword> findAll() {
        return keywordRepository.findAll(); // 모든 키워드를 조회하여 반환
    }

    // 특정 사용자의 키워드를 조회하는 메서드
    public List<Keyword> findByUser(Users user) {
//        return keywordRepository.findByUser(user); // 특정 사용자의 키워드를 조회하여 반환
        return keywordRepository.findByUserOrderByKeywordAsc(user); // 가나다순으로 정렬
    }
}
