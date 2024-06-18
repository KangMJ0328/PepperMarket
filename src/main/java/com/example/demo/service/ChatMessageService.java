package com.example.demo.service;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Users;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // 서비스 클래스임을 나타냄
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Autowired // 생성자 주입
    public ChatMessageService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    // 채팅 메시지를 저장하는 메서드
    public ChatMessage saveChatMessage(ChatMessage chatMessage) {
        // 발신자와 수신자를 데이터베이스에서 조회
        Users sender = userRepository.findById(chatMessage.getSender().getId()).orElseThrow();
        Users receiver = userRepository.findById(chatMessage.getReceiver().getId()).orElseThrow();

        // 발신자와 수신자를 설정
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);

        // 메시지를 저장하고 반환
        return chatMessageRepository.save(chatMessage);
    }

    // 특정 채팅방 ID에 속한 메시지를 타임스탬프 순서대로 가져오는 메서드
    public List<ChatMessage> getMessagesByChatRoomId(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    // 특정 수신자가 받은 모든 메시지를 가져오는 메서드
    public List<ChatMessage> getMessagesByReceiver(Users receiver) {
        return chatMessageRepository.findByReceiver(receiver);
    }

    // 특정 발신자 또는 수신자가 포함된 모든 메시지를 가져오는 메서드
    public List<ChatMessage> getMessagesBySenderOrReceiver(Users sender, Users receiver) {
        return chatMessageRepository.findBySenderOrReceiver(sender, receiver);
    }

    // 사용자가 참여한 채팅방과 각 채팅방의 마지막 메시지를 가져오는 메서드
    public List<ChatMessage> getChatRoomsAndLastMessagesByUser(Users user) {
        // 사용자가 발신한 메시지와 수신한 메시지를 가져옴
        List<ChatMessage> sentMessages = chatMessageRepository.findBySender(user);
        List<ChatMessage> receivedMessages = chatMessageRepository.findByReceiver(user);

        // 채팅방 ID를 키로, 마지막 메시지를 값으로 하는 맵 생성
        Map<Long, ChatMessage> chatRooms = new HashMap<>();

        // 발신한 메시지 처리
        for (ChatMessage message : sentMessages) {
            // 맵에 채팅방 ID가 없으면 메시지를 추가
            chatRooms.putIfAbsent(message.getChatRoomId(), message);
            // 현재 저장된 메시지보다 최신 메시지면 업데이트
            if (message.getTimestamp().isAfter(chatRooms.get(message.getChatRoomId()).getTimestamp())) {
                chatRooms.put(message.getChatRoomId(), message);
            }
        }

        // 수신한 메시지 처리
        for (ChatMessage message : receivedMessages) {
            // 맵에 채팅방 ID가 없으면 메시지를 추가
            chatRooms.putIfAbsent(message.getChatRoomId(), message);
            // 현재 저장된 메시지보다 최신 메시지면 업데이트
            if (message.getTimestamp().isAfter(chatRooms.get(message.getChatRoomId()).getTimestamp())) {
                chatRooms.put(message.getChatRoomId(), message);
            }
        }

        // 마지막 메시지를 리스트로 변환하여 반환
        return new ArrayList<>(chatRooms.values());
    }
}
