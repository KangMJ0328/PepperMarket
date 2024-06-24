package com.example.demo.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrincipalDetails implements UserDetails, OAuth2User {

    @Getter
    private Users users;
    private Map<String, Object> attributes;

    public PrincipalDetails() {
        // 기본 생성자 내용 (필요한 경우)
    }

    public PrincipalDetails(Users users) {
        this.users = users;
    }

    public PrincipalDetails(Users users, Map<String, Object> attributes) {
        this.users = users;
        this.attributes = attributes;
    }

    public Long getId() {
        return users.getId();
    }

    public Users getUser() {
        return users;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(
                new SimpleGrantedAuthority(users.getRole().getAuthority()),
                new SimpleGrantedAuthority("user")
        ).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return users.getNickname();
    }

    public String getNickname() {
        return users.getNickname();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }


    public String getProfilePic() {
        return users.getProfilePicPath();
    }

    public String getRole() {
        return users.getRole().getAuthority();
    }
}
