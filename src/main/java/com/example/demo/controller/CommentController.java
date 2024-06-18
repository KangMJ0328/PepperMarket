package com.example.demo.controller;

import com.example.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 삭제 처리 메서드
    @PostMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Integer boardId = commentService.getBoardIdByCommentId(id);
        if (commentService.isCommentOwner(id, userDetails.getUsername())) {
            commentService.deleteCommentById(id); // 댓글 삭제
            model.addAttribute("message", "댓글이 삭제되었습니다.");
            model.addAttribute("redirectUrl", "/board/view?id=" + boardId);
        } else {
            model.addAttribute("message", "작성자만 댓글을 삭제할 수 있습니다.");
            model.addAttribute("redirectUrl", "/board/view?id=" + boardId);
        }
        return "message";
    }
}
