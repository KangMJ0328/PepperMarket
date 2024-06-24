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
	
	
}