package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {

    public static final List<String> categoryList = Arrays.asList(
            "남성의류", "여성의류", "잡화", "디지털",
            "취미/게임", "스포츠/레저", "가구/인테리어", "반려동물/식물"
    );

    public List<String> getCategoryList() {
        return categoryList;
    }
}
