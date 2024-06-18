package com.example.demo.service;

import com.example.demo.entity.Board;
import com.example.demo.entity.Like;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BoardRepository boardRepository;

    public boolean addLike(Long boardId, String userEmail) {
        Integer boardIdInt = boardId.intValue(); // Long을 Integer로 변환
        logger.info("addLike 호출됨: boardId={}, userEmail={}", boardIdInt, userEmail);
        Optional<Like> existingLike = likeRepository.findByBoardIdAndUserEmail(boardIdInt, userEmail);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            updateLikeCount(boardIdInt);
            logger.info("좋아요 취소됨: boardId={}, userEmail={}", boardIdInt, userEmail);
            return false; // 좋아요 취소
        } else {
            Like like = new Like();
            like.setBoardId(boardIdInt);
            like.setUserEmail(userEmail);
            likeRepository.save(like);
            updateLikeCount(boardIdInt);
            logger.info("좋아요 추가됨: boardId={}, userEmail={}", boardIdInt, userEmail);
            return true; // 좋아요 추가
        }
    }

    public long getLikeCount(Long boardId) {
        Integer boardIdInt = boardId.intValue(); // Long을 Integer로 변환
        long count = likeRepository.countByBoardId(boardIdInt);
        logger.info("getLikeCount 호출됨: boardId={}, count={}", boardIdInt, count);
        return count;
    }

    public boolean hasUserLiked(Long boardId, String userEmail) {
        Integer boardIdInt = boardId.intValue(); // Long을 Integer로 변환
        boolean hasLiked = likeRepository.findByBoardIdAndUserEmail(boardIdInt, userEmail).isPresent();
        logger.info("hasUserLiked 호출됨: boardId={}, userEmail={}, hasLiked={}", boardIdInt, userEmail, hasLiked);
        return hasLiked;
    }

    private void updateLikeCount(Integer boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Invalid board ID"));
        board.setLikecount((int) likeRepository.countByBoardId(boardId));
        boardRepository.save(board);
        logger.info("updateLikeCount 호출됨: boardId={}", boardId);
    }
}
