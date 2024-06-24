package com.example.demo.controller;


import com.example.demo.dto.ReportRequest;
import com.example.demo.entity.Board;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Users;
import com.example.demo.entity.ViewedPost;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ViewedPostService viewedPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private LikeService likeService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    public BoardController(BoardService boardService, CommentService commentService, NotificationService notificationService) {
        this.boardService = boardService;
        this.commentService = commentService;
        this.notificationService = notificationService;
    }

    @GetMapping("/board/write")
    public String boardWriteForm(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        model.addAttribute("username", email);
        return "boardWrite";
    }
    @GetMapping("/board/list")
    public String boardList(Model model,
                            @PageableDefault(page = 0, size = 12) Pageable pageable,
                            String searchKeyword,
                            @RequestParam(required = false) Integer searchCateID,
                            @RequestParam(required = false) boolean showCompleted,
                            @RequestParam(required = false) boolean ajax,
                            @RequestParam(defaultValue = "createDate") String sortBy,  // 기본 정렬 기준
                            @RequestParam(defaultValue = "DESC") String direction,  // 기본 정렬 방향
                            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("boardList method called with: searchKeyword={}, searchCateID={}, showCompleted={}, sortBy={}, direction={}",
                searchKeyword, searchCateID, showCompleted, sortBy, direction);
        
        Page<Board> list = boardService.searchBoards(searchKeyword, searchCateID, pageable, showCompleted, userDetails, sortBy, direction);
        
        model.addAttribute("list", list);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        
        logger.info("List size: " + list.getTotalElements());
        
        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());
        
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPage", list.getTotalPages());
        
        List<String> categList = categoryService.getCategoryList();
        Map<Integer, Long> categoryPostCounts = boardService.getCategoryPostCounts();
        String categNow;
        if (searchCateID == null) {
            categNow = "전체 상품";
        } else {
            categNow = categList.get(searchCateID - 1);
        }
        model.addAttribute("categNow", categNow);
        model.addAttribute("categList", categList);
        model.addAttribute("categoryPostCounts", categoryPostCounts);
        
        Long loggedUserId = getCurrentUserId();
        model.addAttribute("loggedUserId", loggedUserId);
        
        logger.info("Rendering boardList template");
        if (ajax) {
            long totalElements = list.getTotalElements();
            model.addAttribute("totalElements", totalElements);
            // Ajax 요청인 경우, 프래그먼트를 반환하여 부분적으로 업데이트합니다.
            return "boardLists :: content"; // 이 뷰 템플릿은 상품 목록에 해당하는 부분만을 포함해야 합니다.
        } else {
            long totalElements = list.getTotalElements();
            model.addAttribute("totalElements", totalElements);
            // 판매 완료된 상품을 보여주지 않는 경우, 기존의 뷰 템플릿을 반환합니다.
            return "boardLists";
        }
    }
    
    
    
    @GetMapping("/board/view")
    public String boardView(Model model, @RequestParam("id") Integer id, @AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }

        logger.info("boardView 호출됨: id={}, userDetails={}", id, userDetails);

        // 세션에서 좋아요 요청 플래그 확인
        Boolean likeRequest = (Boolean) session.getAttribute("likeRequest");
        if (likeRequest == null || !likeRequest) {
            boardService.boardViewCount(id); // 조회수 증가 로직
        } else {
            session.removeAttribute("likeRequest"); // 플래그 초기화
        }

        Board board = boardService.boardView(id);
        model.addAttribute("board", board);
        List<Comment> comments = commentService.getCommentsByBoardId(id);
        model.addAttribute("comments", comments);

        if (userDetails != null) {
            Users currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            model.addAttribute("loggedInUser", currentUser);

            // 사용자 좋아요 여부 추가
            boolean liked = likeService.hasUserLiked(id.longValue(), userDetails.getUsername());
            model.addAttribute("liked", liked);
        } else {
            model.addAttribute("liked", false);
        }

        Long userId = getCurrentUserId();
        viewedPostService.addViewedPost(userId, Long.valueOf(id));

        String writer = board.getUser().getName();
        model.addAttribute("writer", writer);
        String writerPic = board.getUser().getProfilePictureUrl();
        model.addAttribute("writerPic", writerPic);

        Long userPostCount = boardService.getBoardCountByUserId(board.getUser().getId());
        model.addAttribute("userPostCount", userPostCount);
        // 좋아요 수 추가
        long likeCount = board.getLikecount() == null ? 0 : board.getLikecount();
        model.addAttribute("likeCount", likeCount);

        // *** 신고하기 기능 추가 ***
        model.addAttribute("reportRequest", new ReportRequest());

        return "boardView";
    }

    private Long getCurrentUserId() {
        // 실제 사용자 ID를 가져오는 로직으로 대체해야 합니다.
        // 여기서는 예시로 1L을 반환합니다. 실제 구현에서는 인증된 사용자 정보에서 ID를 가져와야 합니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            Users user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        return 1L;
    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("board", boardService.boardView(id));
        return "boardmodify";
    }

    @PostMapping("/board/writepro")
    public String boardWritePro(@RequestParam("cateID") String cateID, Board board, Model model, MultipartFile file) throws Exception {
        logger.info("게시글 작성 시작");

        // Validate and convert cateID
        try {
            if (cateID == null || cateID.isEmpty() || cateID.equals("none")) {
                throw new NumberFormatException("Category ID is invalid");
            }
            int parsedCateID = Integer.parseInt(cateID);
            board.setCateID(parsedCateID);
        } catch (NumberFormatException e) {
            logger.error("카테고리 ID 변환 중 오류 발생", e);
            model.addAttribute("message", "유효한 카테고리 ID를 선택하십시오.");
            model.addAttribute("redirectUrl", "/board/write");
            return "message";
        }

        board.setViewcount(0);
        board.setCreateDate(LocalDateTime.now());

        try {
            Board savedBoard = boardService.write(board, file);
            logger.info("게시글 저장 완료: " + savedBoard.getId());

            notificationService.notify(savedBoard);
            logger.info("알림 생성 완료");

            model.addAttribute("message", "게시글 작성 완료했습니다.");
            model.addAttribute("redirectUrl", "/board/view?id=" + savedBoard.getId());
            return "message";
        } catch (Exception e) {
            logger.error("게시글 작성 중 오류 발생", e);
            model.addAttribute("message", "게시글 작성 중 오류가 발생했습니다.");
            model.addAttribute("redirectUrl", "/board/write");
            return "message";
        }
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, Model model, MultipartFile file) throws Exception {
        Board boardTemp = boardService.boardView(id);

        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());
        boardTemp.setPrice(board.getPrice());
        boardTemp.setModifyDate(LocalDateTime.now());
        boardTemp.setStatus(board.getStatus());

        boardService.write(boardTemp, file);

        model.addAttribute("message", "글 수정이 완료되었습니다.");
        model.addAttribute("redirectUrl", "/board/view?id=" + id);
        return "message";
    }

    @PostMapping("/board/add-comment/{boardId}")
    public String addComment(@PathVariable Integer boardId, @AuthenticationPrincipal UserDetails userDetails, @RequestParam String content) {
        commentService.addComment(boardId, content, userDetails);
        return "redirect:/board/view?id=" + boardId;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        logger.warn("파일 업로드 크기 초과", e);
        redirectAttributes.addFlashAttribute("message", "파일 업로드 크기를 초과했습니다. 최대 파일 크기는 10MB입니다.");
        return "redirect:/board/write";
    }

    @GetMapping("/board/delete/{id}")
    public String boardDelete(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Board board = boardService.boardView(id);
        Users currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

        if (currentUser == null || (!board.getUser().getEmail().equals(userDetails.getUsername()) && !"admin".equals(currentUser.getNickname()))) {
            model.addAttribute("message", "작성자 또는 관리자로 로그인한 경우에만 글을 삭제할 수 있습니다.");
            model.addAttribute("redirectUrl", "/board/list");
            return "message";
        }

        try {
            // 게시글 삭제
            boardService.boardDelete(id);

            model.addAttribute("message", "게시글 삭제 완료했습니다.");
            model.addAttribute("redirectUrl", "/board/list");
        } catch (Exception e) {
            model.addAttribute("message", "게시글 삭제 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("redirectUrl", "/board/list");
        }

        return "message";
    }

    @GetMapping("/board/recentViewedPosts")
    public String recentViewedPosts(Model model) {
        Long userId = getCurrentUserId();
        List<ViewedPost> viewedPosts = viewedPostService.getRecentViewedPosts(userId);
        model.addAttribute("viewedPosts", viewedPosts);
        return "recentViewedPosts";
    }

    @PostMapping("/board/like")
    @ResponseBody // JSON 응답을 위해 필요
    public ResponseEntity<String> likeBoard(@RequestParam Integer boardId, @AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        logger.info("likeBoard 호출됨: boardId={}, userDetails={}", boardId, userDetails);

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = userDetails.getUsername();
        boolean liked = likeService.addLike(boardId.longValue(), userEmail);

        // 좋아요 상태 메시지 반환
        String message = liked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.";

        // 좋아요 요청 플래그 설정 (세션 사용 대신 JSON 응답에 포함할 수 있음)
        session.setAttribute("likeRequest", true); // 세션 사용 예시

        // JSON 형식의 응답 반환
        return ResponseEntity.ok(message);
    }

    @GetMapping("/board/like")
    @ResponseBody
    public Map<String, Boolean> hasUserLiked(@RequestParam Integer boardId, @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("hasUserLiked 호출됨: boardId={}, userDetails={}", boardId, userDetails);
        Map<String, Boolean> response = new HashMap<>();
        if (userDetails == null) {
            response.put("liked", false);
        } else {
            boolean liked = likeService.hasUserLiked(boardId.longValue(), userDetails.getUsername());
            response.put("liked", liked);
        }
        return response;
    }

    @GetMapping("/board/like/count")
    @ResponseBody
    public Long getLikeCount(@RequestParam Integer boardId) {
        logger.info("getLikeCount 호출됨: boardId={}", boardId);
        return likeService.getLikeCount(boardId.longValue());
    }
}



