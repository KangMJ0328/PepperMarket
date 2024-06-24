package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Users;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(notificationService.findByUser(user));
    }

    // @GetMapping("/popup") 제거한 이유:
    // 별도의 팝업버튼으로 알림 리스트를 가져오는 엔드포인트가 필요하지 않기 때문에 제거
    // 모든 알림을 가져오는 기능은 @GetMapping 엔드포인트에서 처리
    // 만약 팝업버튼에서 알림 리스트를 가져오는 기능이 필요, 기존의 @GetMapping 엔드포인트를 사용

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/readtoggle")
    public ResponseEntity<Void> markAsReadToggle(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        if (notification.isRead()) {
            notificationService.markAsUnRead(id);
        } else {
            notificationService.markAsRead(id);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteByNotificationId(@PathVariable Long id) {
        notificationService.deleteNoti(id);
        return ResponseEntity.noContent().build();
    }
}
