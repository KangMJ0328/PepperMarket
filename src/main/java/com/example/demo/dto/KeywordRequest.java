package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordRequest {
    private Long userId;
    private String keyword;
}