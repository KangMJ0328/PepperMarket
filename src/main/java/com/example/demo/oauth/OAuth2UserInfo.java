package com.example.demo.oauth;

public interface OAuth2UserInfo {
    String getProviderId();

    String getProvider();

    String getEmail();

    String getName();

    // 추가 - 프로필 사진 URL 반환
    String getProfilePictureUrl();
}