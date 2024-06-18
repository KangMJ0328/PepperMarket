package com.example.demo.repository;

import com.example.demo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
    ChatRoom findBySenderIdAndReceiverIdAndBoardId(Long senderId, Long receiverId, Long boardId);
    void deleteByBoardId(Integer boardId);
}
