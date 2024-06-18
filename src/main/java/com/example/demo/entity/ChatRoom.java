package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long senderId;

    @Column
    private Long receiverId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Board board;

    public ChatRoom() {
    }

    public ChatRoom(Long senderId, Long receiverId, Board board) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.board = board;
    }

    public Long getPostId() {
        if (this.board == null) {
            throw new IllegalStateException("Board is not initialized");
        }
        return Long.valueOf(board.getId());
    }

    public String getPostPic() {
        return board.getFilepath();
    }
}
