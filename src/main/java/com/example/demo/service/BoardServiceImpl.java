package com.example.demo.service;

import com.example.demo.entity.Board;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl extends BoardService {
	
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private LikeService likeService;
	
	public BoardServiceImpl(UserRepository userRepository) {
		super(userRepository);
	}
	
	@Override
	public Page<Board> searchBoards(String searchKeyword, Integer searchCateID, Pageable pageable, boolean showCompleted, UserDetails userDetails) {
		Integer status = showCompleted ? null : 3; // showCompleted가 true이면 status를 null로 설정하여 모든 상태의 게시글을 가져옴
		Page<Board> boards = boardRepository.searchBoards(searchKeyword, searchCateID, status, pageable);
		
		String username = userDetails != null ? userDetails.getUsername() : null;
		
		for (Board board : boards) {
			boolean liked = username != null && likeService.hasUserLiked(Long.valueOf(board.getId()), username);
			board.setLiked(liked);
		}
		
		return boards;
	}
}