package com.example.demo.controller;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    @Autowired
    public ChatMessageController(ChatMessageService chatMessageService, UserRepository userRepository) {
        this.chatMessageService = chatMessageService;
        this.userRepository = userRepository;
    }

    // 클라이언트가 "/chat.sendMessage"로 메시지를 보낼 때 호출되는 메서드
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now()); // 현재 시간을 타임스탬프로 설정

        // 발신자와 수신자를 데이터베이스에서 찾음
        Users sender = userRepository.findById(chatMessage.getSenderId()).orElseThrow(() -> new IllegalArgumentException("Invalid sender ID"));
        Users receiver = userRepository.findById(chatMessage.getReceiverId()).orElseThrow(() -> new IllegalArgumentException("Invalid receiver ID"));

        chatMessage.setSender(sender); // 발신자 설정
        chatMessage.setReceiver(receiver); // 수신자 설정

        // 메시지를 저장하고 반환
        return chatMessageService.saveChatMessage(chatMessage);
    }

    // 클라이언트가 "/chat.addUser"로 사용자 추가 요청을 보낼 때 호출되는 메서드
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(ChatMessage chatMessage) {
        // 사용자 추가 메시지 내용 설정
        chatMessage.setContent(chatMessage.getSender().getNickname() + " joined the chat");
        chatMessage.setTimestamp(LocalDateTime.now()); // 현재 시간을 타임스탬프로 설정

        // 발신자를 데이터베이스에서 찾음
        Users sender = userRepository.findById(chatMessage.getSenderId()).orElseThrow(() -> new IllegalArgumentException("Invalid sender ID"));
        chatMessage.setSender(sender); // 발신자 설정

        // 메시지를 저장하고 반환
        return chatMessageService.saveChatMessage(chatMessage);
    }
}