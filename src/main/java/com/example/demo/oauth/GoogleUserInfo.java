package com.example.demo.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 구글 사용자 정보를 나타내는 클래스입니다.
 */
@Getter
@Setter
@AllArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes; // 구글에서 받은 사용자 정보

    // OAuth2UserInfo 인터페이스의 추상 메서드 구현

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub"); // 구글 제공자 ID 반환
    }

    @Override
    public String getProvider() {
        return "google"; // 제공자를 나타내는 문자열 반환
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email"); // 이메일 주소 반환
    }

    @Override
    public String getName() {
        return (String) attributes.get("name"); // 이름 반환
    }

    //  프로필 사진 URL 반환
    public String getProfilePictureUrl() {
        // 프로필 사진 URL은 Google에서 제공하는 'picture' 속성에 있을 수 있습니다.
        // 속성이 없는 경우 기본값이나 null을 반환할 수 있습니다.
        return (String) attributes.get("picture");
    }
}
