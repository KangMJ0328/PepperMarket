package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 댓글을 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    private String content;

    private LocalDateTime createDate;
    public Comment() {
        this.createDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }
    public String getFormattedCreateDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return this.createDate.format(formatter);
    }

    // 작성자 닉네임 추가
    private String nickname;


}
