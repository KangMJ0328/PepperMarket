package com.example.demo.controller;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BoardService;
import com.example.demo.service.ChatMessageService;
import com.example.demo.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final BoardService boardService;

    @Autowired
    public ChatController(ChatMessageService chatMessageService, UserRepository userRepository, ChatRoomService chatRoomService, BoardService boardService) {
        this.chatMessageService = chatMessageService;
        this.userRepository = userRepository;
        this.chatRoomService = chatRoomService;
        this.boardService = boardService;
    }

    @GetMapping("/start")
    public ModelAndView startChat(@RequestParam Long receiverId, @RequestParam Long postId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long senderId = principalDetails.getUser().getId();
        ChatRoom chatRoom = chatRoomService.createOrGetChatRoom(senderId, receiverId, postId);
        Long chatRoomId = chatRoom.getId();
        return new ModelAndView("redirect:/chat/room/" + chatRoomId + "?receiverId=" + receiverId);
    }

    @GetMapping("/room/{chatRoomId}")
    public ModelAndView getChatRoom(@PathVariable Long chatRoomId, @RequestParam Long receiverId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        ModelAndView modelAndView = new ModelAndView("chatRoom");
        modelAndView.addObject("chatRoomId", chatRoomId);
        modelAndView.addObject("receiverId", receiverId);
        modelAndView.addObject("userId", principalDetails.getUser().getId());
        return modelAndView;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestParam Long chatRoomId, @RequestParam Long receiverId, @RequestParam String content, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            Long senderId = principalDetails.getUser().getId();
            Users sender = userRepository.findById(senderId).orElseThrow(() -> new IllegalArgumentException("Invalid sender ID"));
            Users receiver = userRepository.findById(receiverId).orElseThrow(() -> new IllegalArgumentException("Invalid receiver ID"));

            ChatMessage message = new ChatMessage();
            message.setChatRoomId(chatRoomId);
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());

            chatMessageService.saveChatMessage(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long chatRoomId) {
        try {
            List<ChatMessage> messages = chatMessageService.getMessagesByChatRoomId(chatRoomId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chats")
    public ResponseEntity<List<Map<String, String>>> getUserChatRooms(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            Long userId = principalDetails.getUser().getId();
            Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
            List<ChatRoom> chatRooms = chatRoomService.findByUserId(userId);

            List<Map<String, String>> chatRoomInfos = chatRooms.stream().map(room -> {
                Map<String, String> chatRoomInfo = new HashMap<>();
                chatRoomInfo.put("chatRoomId", room.getId().toString());
                chatRoomInfo.put("partnerName", getPartnerName(room, userId));
                chatRoomInfo.put("partnerId", String.valueOf(room.getReceiverId().equals(userId) ? room.getSenderId() : room.getReceiverId()));
                chatRoomInfo.put("postId", String.valueOf(room.getPostId()));

                String postTitle = boardService.getBoardTitleByPostId(room.getPostId());
                chatRoomInfo.put("postTitle", postTitle);
                return chatRoomInfo;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(chatRoomInfos);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/chatRoom/{chatRoomId}")
    public ResponseEntity<Map<String, Object>> getChatRoomInfo(@PathVariable Long chatRoomId) {
        try {
            Optional<ChatRoom> chatRoom = chatRoomService.findByChatRoomId(chatRoomId);
            if (chatRoom.isPresent()) {
                ChatRoom room = chatRoom.get();
                Map<String, Object> chatRoomInfo = new HashMap<>();
                chatRoomInfo.put("partnerName", getPartnerName(room, room.getSenderId()));
                chatRoomInfo.put("postId", room.getPostId());
                chatRoomInfo.put("postCount", getPartnerPostCount(room, room.getSenderId()));
                chatRoomInfo.put("postTitle", boardService.getBoardTitleByPostId(room.getPostId()));
                chatRoomInfo.put("postPic", room.getPostPic());
                chatRoomInfo.put("partnerId", String.valueOf(room.getReceiverId().equals(room.getSenderId()) ? room.getSenderId() : room.getReceiverId()));

                return ResponseEntity.ok(chatRoomInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getPartnerName(ChatRoom room, Long userId) {
        Long partnerId = room.getSenderId().equals(userId) ? room.getReceiverId() : room.getSenderId();
        return userRepository.findById(partnerId).map(Users::getNickname).orElse("Unknown");
    }
    private Long getPartnerPostCount(ChatRoom room, Long userId) {
        Long partnerId = room.getSenderId().equals(userId) ? room.getReceiverId() : room.getSenderId();
        return boardService.getBoardCountByUserId(partnerId);
    }
}
