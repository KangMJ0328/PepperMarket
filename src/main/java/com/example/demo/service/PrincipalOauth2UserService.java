package com.example.demo.service;

import com.example.demo.entity.PrincipalDetails;
import com.example.demo.entity.UserRole;
import com.example.demo.entity.Users;
import com.example.demo.oauth.GoogleUserInfo;
import com.example.demo.oauth.NaverUserInfo;
import com.example.demo.oauth.OAuth2UserInfo;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo;
        if (provider.equals("google")) {
            userInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("naver")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            userInfo = new NaverUserInfo(response);
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        return handleLogin(oAuth2User, userInfo);
    }

    private OAuth2User handleLogin(OAuth2User oAuth2User, OAuth2UserInfo userInfo) {
        String provider = userInfo.getProvider();
        String providerId = userInfo.getProviderId();
        String email = userInfo.getEmail();
        String socialId = provider + "_" + providerId;
        String nickname = email.split("@")[0];
        String profilePictureUrl = userInfo.getProfilePictureUrl();

        Users user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, profilePictureUrl))
                .orElseGet(() -> createNewUser(email, provider, providerId, socialId, nickname, profilePictureUrl));

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    private Users updateExistingUser(Users user, String profilePictureUrl) {
        user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }

    private Users createNewUser(String email, String provider, String providerId, String socialId, String nickname, String profilePictureUrl) {
        Users user = Users.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(""))
                .provider(provider)
                .providerId(providerId)
                .socialId(socialId)
                .nickname(nickname)
                .profilePictureUrl(profilePictureUrl)
                .role(UserRole.USER) // 기본 역할 설정
                .build();
        return userRepository.save(user);
    }
}
