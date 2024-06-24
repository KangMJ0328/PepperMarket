package com.example.demo.controller;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Users;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public ChatMessageController(ChatMessageService chatMessageService, UserRepository userRepository) {
        this.chatMessageService = chatMessageService;
        this.userRepository = userRepository;
    }

    // 클라이언트가 "/chat.sendMessage"로 메시지를 보낼 때 호출되는 메서드 (WebSocket)
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessageViaWebSocket(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now()); // 현재 시간을 타임스탬프로 설정

        // 발신자와 수신자를 데이터베이스에서 찾음
        Users sender = userRepository.findById(chatMessage.getSenderId()).orElseThrow(() -> new IllegalArgumentException("Invalid sender ID"));
        Users receiver = userRepository.findById(chatMessage.getReceiverId()).orElseThrow(() -> new IllegalArgumentException("Invalid receiver ID"));

        chatMessage.setSender(sender); // 발신자 설정
        chatMessage.setReceiver(receiver); // 수신자 설정

        // 메시지를 저장하고 반환
        ChatMessage savedMessage = chatMessageService.saveChatMessage(chatMessage);

        // 알림 생성
        createNotification(savedMessage);

        return savedMessage;
    }

    private void createNotification(ChatMessage chatMessage) {
        Notification notification = new Notification();
        notification.setMessage(chatMessage.getContent());
        notification.setRead(false);
        notification.setChatMessage(chatMessage);
        notification.setReceiver(chatMessage.getReceiver());
        notificationRepository.save(notification);

        // 로그 추가
        System.out.println("알림 생성: " + notification.getMessage());
    }

    // 클라이언트가 "/chat.addUser"로 사용자 추가 요청을 보낼 때 호출되는 메서드 (WebSocket)
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

    // REST API를 통한 메시지 전송 메서드
    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage chatMessage) {
        chatMessage.setRead(false);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 알림 생성
        createNotification(savedMessage);

        return ResponseEntity.ok(savedMessage);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Optional<ChatMessage> chatMessageOptional = chatMessageRepository.findById(id);
        if (chatMessageOptional.isPresent()) {
            ChatMessage chatMessage = chatMessageOptional.get();
            chatMessage.setRead(true);
            chatMessageRepository.save(chatMessage);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/markAsReadByRoom")
    public ResponseEntity<Void> markAsReadByRoom(@RequestParam Long roomId, @RequestParam Long receiverId) {
        Users receiver = userRepository.findById(receiverId).orElseThrow(() -> new IllegalArgumentException("Invalid receiver ID"));
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdAndReceiverAndIsRead(roomId, receiver, false);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.setRead(true);
            chatMessageRepository.save(chatMessage);

            // 관련 알림도 읽음 처리
            List<Notification> notifications = notificationRepository.findByChatMessageAndReceiver(chatMessage, receiver);
            for (Notification notification : notifications) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
        return ResponseEntity.ok().build();
    }
}
