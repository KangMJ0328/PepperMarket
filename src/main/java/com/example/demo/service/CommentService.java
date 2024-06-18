package com.example.demo.service;

import com.example.demo.entity.Board;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Users;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service // 서비스 클래스임을 나타냄
public class CommentService {

    @Autowired
    private CommentRepository commentRepository; // 댓글 관련 데이터 처리를 위한 리포지토리

    @Autowired
    private BoardRepository boardRepository; // 게시판 관련 데이터 처리를 위한 리포지토리

    @Autowired
    private UserRepository userRepository; // 사용자 관련 데이터 처리를 위한 리포지토리

    // 특정 게시글 ID에 해당하는 모든 댓글을 가져오는 메서드
    public List<Comment> getCommentsByBoardId(Integer boardId) {
        return commentRepository.findByBoardId(boardId);
    }

    // 댓글을 추가하는 메서드
    public void addComment(Integer boardId, String content, @AuthenticationPrincipal UserDetails userDetails) {
        // 게시글을 ID로 검색, 없으면 예외 발생
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Invalid board Id"));
        // 사용자를 이메일로 검색, 없으면 예외 발생
        Users user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        // 새로운 댓글 객체 생성 및 설정
        Comment comment = new Comment();
        comment.setBoard(board); // 댓글이 속한 게시글 설정
        comment.setUser(user); // 댓글 작성자 설정
        comment.setContent(content); // 댓글 내용 설정
        comment.setCreateDate(LocalDateTime.now()); // 댓글 작성 시간 설정
        comment.setNickname(user.getNickname()); // 댓글 작성자의 닉네임 설정

        // 댓글 저장
        commentRepository.save(comment);
    }

    // 특정 게시글 ID에 해당하는 모든 댓글을 삭제하는 메서드
    @Transactional
    public void deleteCommentsByBoardId(Integer boardId) {
        commentRepository.deleteByBoardId(boardId);
    }

    // 특정 댓글을 ID로 삭제하는 메서드
    @Transactional
    public void deleteCommentById(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

    // 댓글 작성자 확인 메서드
    public boolean isCommentOwner(Integer commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));
        return comment.getUser().getEmail().equals(username);
    }

    // 댓글 ID로 게시글 ID를 가져오는 메서드 추가
    public Integer getBoardIdByCommentId(Integer commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));
        return comment.getBoard().getId();
    }
}
