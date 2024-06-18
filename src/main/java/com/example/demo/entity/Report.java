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
    private String reportedUser;  // 신고된 사용자 이메일

    @Column(nullable = false)
    private String reason;  // 신고 이유

    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedAt;  // 신고 날짜
}
