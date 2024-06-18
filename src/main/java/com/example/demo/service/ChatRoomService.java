package com.example.demo.service;

import com.example.demo.entity.Board;
import com.example.demo.entity.ChatRoom;
import com.example.demo.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final BoardService boardService;

    @Autowired
    public ChatRoomService(ChatRoomRepository chatRoomRepository, BoardService boardService) {
        this.chatRoomRepository = chatRoomRepository;
        this.boardService = boardService;

    }

    public ChatRoom createOrGetChatRoom(Long senderId, Long receiverId, Long postId) {
        // 기존 채팅방이 있는지 확인
        ChatRoom existingChatRoom = chatRoomRepository.findBySenderIdAndReceiverIdAndBoardId(senderId, receiverId, postId);
        if (existingChatRoom != null) {
            return existingChatRoom;
        }

        // 없으면 새로운 채팅방 생성
        Board board = boardService.getBoardById(postId);
        ChatRoom chatRoom = new ChatRoom(senderId, receiverId, board);
        return chatRoomRepository.save(chatRoom);
    }


    public List<ChatRoom> findByUserId(Long userId) {
        return chatRoomRepository.findBySenderIdOrReceiverId(userId, userId);
    }
    @Transactional
    public void deleteChatRoomByBoardId(Integer boardId) {
        chatRoomRepository.deleteByBoardId(boardId);
    }
    
    public Optional<ChatRoom> findByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId);
    }
}
