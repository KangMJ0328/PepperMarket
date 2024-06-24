package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reporter;  // 신고자 이메일

    @Column(nullable = false)
    private String reason;  // 신고 이유

    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedAt;  // 신고 날짜

    private String reporterEmail;
    private String reportedEmail;

    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private Users reportedUser;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "reported_post_id", nullable = false)
    private Board reportedPost;  // 신고된 게시글 추가
}
