package com.example.demo.service;

import com.example.demo.entity.Keyword;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Users;
import com.example.demo.entity.Board;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // 서비스 클래스임을 나타냄
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository; // 알림 관련 데이터 처리를 위한 리포지토리
    @Autowired
    private KeywordService keywordService; // 키워드 관련 서비스
    @Autowired
    private UserRepository userRepository; // 사용자 관련 데이터 처리를 위한 리포지토리

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 모든 알림을 조회하는 메서드
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    // 특정 사용자의 알림을 조회하는 메서드
    public List<Notification> findByUser(Users user) {
        return notificationRepository.findByUser(user);
    }
    
    public void deleteNoti(Long id) {
        notificationRepository.deleteById(id);
    }
    // 게시글과 관련된 키워드 알림을 생성하는 메서드
    public void notify(Board board) {
        // 모든 사용자를 조회
        List<Users> usersList = userRepository.findAll();

        // 각 사용자에 대해 키워드를 확인
        for (Users user : usersList) {
            List<Keyword> userKeywords = keywordService.findByUser(user);
            logger.info("Checking keywords for user ID: " + user.getId());

            // 각 키워드에 대해 게시글 제목에 포함되어 있는지 확인
            for (Keyword keyword : userKeywords) {
                logger.info("Checking keyword: " + keyword.getKeyword());
                if (board.getTitle().contains(keyword.getKeyword())) {
                    // 키워드가 포함된 경우 알림 생성 및 저장
                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setBoard(board);
                    notification.setMessage(board.getTitle() + " 키워드가 포함된 글이 올라왔어요");
                    notification.setRead(false);
                    notificationRepository.save(notification);
                    logger.info("Notification saved for user ID: " + user.getId() + " for board ID: " + board.getId());
                }
            }
        }
    }

    // 특정 알림을 읽음 상태로 표시하는 메서드
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true); // 알림을 읽음 상태로 설정
        notificationRepository.save(notification); // 알림 저장
    }
    public void markAsUnRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(false); // 알림을 읽음 상태로 설정
        notificationRepository.save(notification); // 알림 저장
    }

    // 특정 게시글과 관련된 알림을 삭제하는 메서드 추가
    @Transactional
    public void deleteNotificationsByBoardId(Integer boardId) {
        notificationRepository.deleteByBoardId(boardId);
    }
    
    
    
    public int getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUnreadNotificationsByUser(userId);
    }
}
