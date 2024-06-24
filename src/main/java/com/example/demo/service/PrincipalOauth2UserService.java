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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    

    public static String getCurrentDateFormatted(String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

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
    
    public byte[] downloadImage(String imageUrl) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new Exception("Failed to download image");
        }
    }
    
    //서버 올릴때 파일 경로 고쳐주세요
    public String saveImageToServer(byte[] imageContent, String email) throws IOException {
        String currentDate = getCurrentDateFormatted("yyyy/MM/dd");
        String profilePath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files\\profile";
        Path directoryPath = Paths.get(profilePath);
        Files.createDirectories(directoryPath);
        String fileName = UUID.randomUUID().toString() + "_" + email + ".jpg";
        Path filePath = Paths.get(profilePath, fileName);
        
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageContent);
        }
        
        return "/files/profile/" + fileName;
    }
    
    
    public Users createNewUser(String email, String provider, String providerId, String socialId, String nickname, String profilePictureUrl) {
        String savedImagePath = null;
        try {
            byte[] imageContent = downloadImage(profilePictureUrl);
            savedImagePath = saveImageToServer(imageContent, email);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
        Users user = Users.builder()
                             .email(email)
                             .password(bCryptPasswordEncoder.encode(""))
                             .provider(provider)
                             .providerId(providerId)
                             .socialId(socialId)
                             .nickname(nickname)
                             .role(UserRole.USER)
                             .profilePictureUrl(profilePictureUrl)
                             .profilePicPath(savedImagePath)
                             .build();
        return userRepository.save(user);
    }
}
