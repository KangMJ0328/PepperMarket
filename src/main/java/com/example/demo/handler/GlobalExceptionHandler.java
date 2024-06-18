package com.example.demo.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    // 파일 업로드 크기 제한 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "File too large!");
        return "redirect:/";
    }

    // 일반 예외 처리 (리다이렉트 용)
    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    public String handleSpecificException(Exception exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "An error occurred: " + exc.getMessage());
        return "redirect:/";
    }

    // 일반 예외 처리 (API 용)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
