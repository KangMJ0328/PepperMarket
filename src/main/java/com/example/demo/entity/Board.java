package com.example.demo.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.demo.service.CategoryService.categoryList;




/**
 * 게시글을 나타내는 엔티티 클래스입니다.
 */

@Entity
@Getter
@Setter
@Data
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Integer 타입으로 수정

    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String price; // 가격
    private String filename; // 파일 이름
    private String filepath; // 파일 경로
    private Integer viewcount; // 조회수
    private Integer likecount; // 좋아요 수
    private Integer cateID; // 카테고리 ID
    private String categName; // 카테고리 이름
    private String quality;
    private Integer status; // 판매 상태 1:판매중, 2:예약중 3:판매완료
    private int likes; // 좋아요 갯수

    @OneToMany(mappedBy = "board")
    private List<ChatRoom> chatRooms;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user; // 게시글 작성자

    @Column(name = "createdate", nullable = true)
    private LocalDateTime createDate; // 생성일

    @Column(name = "modifydate", nullable = true)
    private LocalDateTime modifyDate; // 수정일
    @Getter
    @Setter
    @Transient
    private boolean liked;

    public String getCategName() {
        categName = categoryList.get(cateID - 1);
        return categName;
    }

    public String qualityHangul(){
        return switch (quality) {
            case "perfect" -> "매우 좋음";
            case "good" -> "좋음";
            case "bad" -> "보통";
            default -> " ";
        };
    }

    public Long getWriter(){
        return user.getId();
    }

    public String getWriterPic(){
        return user.getProfilePictureUrl();
    }





//    @ManyToOne
//    @JoinColumn(name = "category_id")
//    private Category category;

    public String getFormattedPrice() {
        if (price == null || price.isEmpty()) {
            return "가격 정보 없음";
        }
        try {
            double priceValue = Double.parseDouble(price);
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            return decimalFormat.format(priceValue);
        } catch (NumberFormatException e) {
            return "잘못된 가격 형식";
        }
    }
    
    // createDate를 기준으로 몇 분 전, 몇 시간 전 등을 반환하는 메서드
    public String getTimeAgo() {
        if (this.createDate == null) {
            return "날짜 정보 없음";
        }
        Date date = Date.from(this.createDate.atZone(ZoneId.systemDefault()).toInstant());
        return Time.calculateTime(date);
    }
    
    // getters and setters...
	
}

