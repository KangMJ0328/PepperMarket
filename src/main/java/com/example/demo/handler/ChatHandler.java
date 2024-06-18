package com.example.demo.handler;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component // Spring에서 빈으로 관리되는 컴포넌트로 지정
@Slf4j // Lombok을 사용하여 로깅 기능 추가
public class ChatHandler extends TextWebSocketHandler {

    // WebSocket 세션을 저장할 맵, 동시성 문제를 해결하기 위해 ConcurrentHashMap 사용
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    private final ChatMessageService chatMessageService; // ChatMessageService를 주입받아 사용
    private final UserRepository userRepository; // UserRepository를 주입받아 사용

    @Autowired // 생성자 주입
    public ChatHandler(ChatMessageService chatMessageService, UserRepository userRepository) {
        this.chatMessageService = chatMessageService;
        this.userRepository = userRepository;
    }

    // WebSocket 연결이 성립되면 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 세션을 맵에 저장
        String sessionId = session.getId();
        sessionMap.put(sessionId, session);
        log.info("세션 연결: {}", sessionId); // 연결된 세션 로그 출력
    }

    // 텍스트 메시지를 수신하면 호출되는 메서드
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 수신한 메시지의 페이로드를 가져옴
        String payload = message.getPayload();
        log.info("수신한 메세지: {}", payload); // 수신한 메시지 로그 출력

        // 예제: payload를 ChatMessage 객체로 변환
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(payload);
        chatMessage.setTimestamp(LocalDateTime.now()); // 현재 시간을 타임스탬프로 설정

        // sender와 receiver 설정 (예제 ID 사용, 실제 사용 시 동적 할당 필요)
        Users sender = userRepository.findById(1L).orElseThrow(); // 예제: ID가 1인 사용자 찾기
        Users receiver = userRepository.findById(2L).orElseThrow(); // 예제: ID가 2인 사용자 찾기
        chatMessage.setSender(sender); // 발신자 설정
        chatMessage.setReceiver(receiver); // 수신자 설정

        // 메시지를 데이터베이스에 저장
        chatMessageService.saveChatMessage(chatMessage);

        // 모든 WebSocket 세션에 메시지 전송
        for (WebSocketSession webSocketSession : sessionMap.values()) {
            webSocketSession.sendMessage(new TextMessage(payload));
        }
    }

    // WebSocket 연결이 종료되면 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션을 맵에서 제거
        String sessionId = session.getId();
        sessionMap.remove(sessionId);
        log.info("세션 종료: {}", sessionId); // 종료된 세션 로그 출력
    }
}
