package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 사용자 등록 요청을 담은 DTO 클래스입니다.
 */
@Getter
@Setter
public class AddUserRequest {

    @Email
    @NotEmpty(message = "이메일을 입력해 주세요.")
    private String email; // 이메일 주소

    @NotEmpty(message = "비밀번호를 입력해 주세요.")
    private String password; // 비밀번호

    @NotEmpty(message = "비밀번호 확인을 입력해 주세요.")
    private String passwordCheck; // 비밀번호 확인
    


    @NotBlank
    private String name; // 이름

}
