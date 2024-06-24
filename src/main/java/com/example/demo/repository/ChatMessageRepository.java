package com.example.demo.repository;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Spring Data JPA의 저장소로 사용
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 사용자가 참여한 채팅방 ID 목록을 조회하는 사용자 정의 쿼리
    @Query("SELECT DISTINCT cm.chatRoomId FROM ChatMessage cm WHERE cm.sender.email = :userEmail OR cm.receiver.email = :userEmail")
    List<String> findChatRoomsByUserEmail(@Param("userEmail") String userEmail);

    // 특정 채팅방 ID에 속한 모든 메시지를 타임스탬프 오름차순으로 조회
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);

    // 발신자 또는 수신자가 특정 사용자와 일치하는 모든 메시지를 조회하는 사용자 정의 쿼리
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sender = :sender OR cm.receiver = :receiver")
    List<ChatMessage> findBySenderOrReceiver(@Param("sender") Users sender, @Param("receiver") Users receiver);

    // 특정 사용자가 수신한 모든 메시지를 조회
    List<ChatMessage> findByReceiver(Users receiver);

    // 특정 사용자가 발신한 모든 메시지를 조회
    List<ChatMessage> findBySender(Users sender);

    List<ChatMessage> findByChatRoomId(Long chatRoomId);

    // 특정 채팅방 ID, 수신자, 읽음 여부로 메시지를 조회하는 메서드 추가
    List<ChatMessage> findByChatRoomIdAndReceiverAndIsRead(Long chatRoomId, Users receiver, boolean isRead);
}
