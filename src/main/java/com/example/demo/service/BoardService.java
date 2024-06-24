package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service // 서비스 클래스임을 나타냄
public abstract class BoardService {

    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeService likeService;

    @Value("${file.upload-dir}")
    private String uploadDir;


    @Autowired
    public BoardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 게시글 작성 및 파일 업로드를 처리하는 메서드
    public Board write(Board board, MultipartFile file) throws Exception {
        // 현재 인증된 사용자 정보를 가져옴
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        Users user = userRepository.findByEmail(username).orElseThrow();

        // 게시글에 사용자 정보와 작성 시간을 설정
        board.setUser(user);
//        if(board.getCreateDate() == null) {
//            board.setCreateDate(LocalDateTime.now());
//        }else{
//            board.setModifyDate(LocalDateTime.now());
//        }
        board.setLikecount(0);
        try {
            // 파일이 비어있지 않으면 파일을 저장
            if (!file.isEmpty()) {
                String savePath = "/home/ec2-user/pepper/files/";
                UUID uuid = UUID.randomUUID();
                String fileName = uuid + "_" + file.getOriginalFilename();
                File saveFile = new File(savePath, fileName);
                logger.info("파일 저장 경로: " + saveFile.getPath());
                file.transferTo(saveFile);
                board.setFilename(fileName);
                board.setFilepath("/files/" + fileName);
                logger.info("파일 업로드 성공: " + fileName);
            }

            // 게시글을 저장하고 저장된 게시글 반환
            Board savedBoard = boardRepository.save(board);
            logger.info("게시글 저장 성공: " + savedBoard.getId());
            return savedBoard;
        } catch (Exception e) {
            throw new Exception("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
    // 페이징을 지원하는 모든 게시글 리스트를 가져오는 메서드
    public Page<Board> boardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }


    // 특정 키워드를 포함하는 게시글 리스트를 페이징하여 가져오는 메서드
    public Page<Board> boardSearchListAvailable(String searchKeyword, Pageable pageable, Integer status) {
        return boardRepository.findByTitleContainingAndStatusNot(searchKeyword, pageable, status);
    }

    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {
        return boardRepository.findByTitleContaining(searchKeyword, pageable);
    }

    // 특정 ID의 게시글을 조회하는 메서드
    public Board boardView(Integer id) {
        return boardRepository.findById(id).orElseThrow();
    }

    // 특정 ID의 게시글을 삭제하는 메서드
    @Transactional
    public void boardDelete(Integer id) {
        try {
            // 자식 레코드 삭제
            chatRoomRepository.deleteByBoardId(id);
            commentRepository.deleteByBoardId(id);
            likeRepository.deleteByBoardId(id);
            notificationRepository.deleteByBoardId(id);
            reportRepository.deleteByReportedPostId(id);
            // 게시글 삭제
            boardRepository.deleteById(id);
        } catch (Exception e) {
            // 예외 기록
            System.out.println("Error deleting board: " + e.getMessage());
            throw e;
        }
    }

    // 특정 카테고리 ID의 게시글 리스트를 페이징하여 가져오는 메서드
    public Page<Board> searchByCateIDAvailable(Integer searchCateID, Pageable pageable, Integer status) {
        return boardRepository.findByCateIDAndStatusNot(searchCateID, pageable, status);
    }

    public Page<Board> searchByCateID(Integer searchCateID, Pageable pageable) {
        return boardRepository.findByCateID(searchCateID, pageable);
    }




    // 특정 키워드와 카테고리 ID를 포함하는 게시글 리스트를 페이징하여 가져오는 메서드
    public Page<Board> searchByKeywordAndCateIDAvailable(String searchKeyword, Integer searchCateID, Pageable pageable, Integer status) {
        return boardRepository.findByTitleContainingAndCateIDAndStatusNot(searchKeyword, searchCateID, pageable, status);
    }

    public Page<Board> searchByKeywordAndCateID(String searchKeyword, Integer searchCateID, Pageable pageable) {
        return boardRepository.findByTitleContainingAndCateID(searchKeyword, searchCateID, pageable);
    }

    // 게시글의 제목과 내용을 확인하여 키워드 알림을 생성하는 메서드
    private void checkForKeywords(Board board) {
        List<Keyword> keywords = keywordRepository.findAll();
        for (Keyword keyword : keywords) {
            if (board.getTitle().contains(keyword.getKeyword()) || board.getContent().contains(keyword.getKeyword())) {
                logger.info("Keyword matched: " + keyword.getKeyword());
                Notification notification = new Notification();
                notification.setMessage("새 게시글에 당신의 키워드 '" + keyword.getKeyword() + "'가 포함되어 있습니다.");
                notification.setUser(keyword.getUser());
                notification.setBoard(board);
                notification.setRead(false);
                notificationRepository.save(notification);
                logger.info("Notification saved for user ID: " + keyword.getUser().getId() + " for board ID: " + board.getId());
            }
        }
    }

    // 게시글을 저장하고 키워드 알림을 확인하는 메서드
    public Board saveBoard(Board board) {
        logger.info("Saving board: " + board.getTitle());
        Board savedBoard = boardRepository.save(board);
        checkForKeywords(savedBoard);
        return savedBoard;
    }
    public void boardViewCount(Integer id) {
        Board board = boardRepository.findById(id).orElse(null);
        if (board != null) {
            board.setViewcount(board.getViewcount() + 1);
            boardRepository.save(board);
        }
    }

    // 조회수가 높은 순서로 모든 게시글을 가져오는 메서드
    public List<Board> getPostsByViewcount() {
        return boardRepository.findByOrderByViewcountDesc();
    }
    public List<Board> getPostsByLiked(){return boardRepository.findByOrderByLikecountDesc();}

    // 조회수가 높은 상위 10개의 게시글을 가져오는 메서드
    public List<Board> getTop10PostsByViewcount() {
        return boardRepository.findTop10ByOrderByViewcountDesc();
    }

    // 카테고리별 게시글 수를 가져오는 메서드
    public Map<Integer, Long> getCategoryPostCounts() {
        List<Object[]> results = boardRepository.findCategoryPostCounts();
        Map<Integer, Long> categoryPostCounts = new HashMap<>();
        for (Object[] result : results) {
            Integer cateID = (Integer) result[0];
            Long count = (Long) result[1];
            categoryPostCounts.put(cateID, count);
        }
        return categoryPostCounts;
    }
    public long getBoardCountByUserId(Long userId) {
        return boardRepository.countByUserId(userId);
    }
    
    public Page<Board> getAvailableBoard8() {
        return boardRepository.findByStatusNotOrderByCreateDateDesc(3, Pageable.ofSize(8));
    }
    
    public Page<Board> getAvailableBoard(Pageable pageable) {
        return boardRepository.findByStatusNotOrderByCreateDateDesc(3, pageable);
    }

    public String findAuthorEmailByPostId(Long postId) {
        Board post = boardRepository.findById(Math.toIntExact(postId)).orElseThrow(() -> new IllegalArgumentException("잘못된 게시글 ID입니다."));
        return post.getUser().getEmail(); // 작성자가 Users 엔티티로서 이메일 필드를 가지고 있다고 가정
    }


    


    public void likePost(Long id) {
        boardRepository.incrementLikes(id);
    }

    public Page<Board> searchBoards(String searchKeyword, Integer searchCateID,
                                    Pageable pageable, boolean showCompleted,
                                    UserDetails userDetails, String sortBy, String direction) {
        Integer status = showCompleted ? null : 3; // showCompleted가 true이면 status를 null로 설정하여 모든 상태의 게시글을 가져옴


        // 기본 정렬 기준 설정
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "createDate");

        // 사용자가 선택한 정렬 기준에 따라 정렬 방식 설정
        if ("likecount".equals(sortBy)) {
            defaultSort = Sort.by(Sort.Direction.fromString(direction), "likecount", "createDate");
        } else if ("viewcount".equals(sortBy)) {
            defaultSort = Sort.by(Sort.Direction.fromString(direction), "viewcount", "createDate");
        } else if ("priceASC".equals(sortBy)) {
            defaultSort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("priceDESC".equals(sortBy)) {
            defaultSort = Sort.by(Sort.Direction.DESC, "price");
        }else if ("createDateAsc".equals(sortBy)) {
            defaultSort = Sort.by(Sort.Direction.ASC, "createDate", "createDate");
        } else if ("createDate".equals(sortBy)) {
            defaultSort = Sort.by(Sort.Direction.DESC, "createDate", "createDate");
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);

        // 검색 키워드에 와일드카드 추가
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            searchKeyword = "%" + searchKeyword + "%";
        }

        Page<Board> boards = boardRepository.searchBoards(searchKeyword, searchCateID, status, sortedPageable);
        
        String username = userDetails != null ? userDetails.getUsername() : null;
        
        for (Board board : boards) {
            boolean liked = username != null && likeService.hasUserLiked(Long.valueOf(board.getId()), username);
            board.setLiked(liked);
        }
        
        return boards;
    }



    public Board getBoardById(Long postId) {
        return boardRepository.findById(Math.toIntExact(postId))
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. postId: " + postId));
    }

    public String getBoardTitleByPostId(Long postId) {
        Board board = boardRepository.findById(Math.toIntExact(postId))
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. postId: " + postId));
        return board.getTitle();
    }

    public Page<Board> getBoardByUserId(Long userId, Pageable pageable) {
        return boardRepository.findByUserId(userId, pageable);
    }
    public List<Board> getBoardByUserId(Long userId){
        return boardRepository.findByUserIdOrderByCreateDateDesc(userId);
    }
    
}
