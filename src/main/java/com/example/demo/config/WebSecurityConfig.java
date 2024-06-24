package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.PrincipalOauth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    private final PrincipalOauth2UserService principalOauth2UserService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:/home/ec2-user/pepper/files/");
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:/home/ec2-user/pepper/files/profile/");
    }

    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> {
            logger.info("Configuring web security to ignore /static/** and /error");
            web.ignoring().requestMatchers("/static/**", "/error");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring HTTP security");
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/", "/login", "/signup", "/user", "/board/list", "/board/view/**", "/main", "/layout", "/img/**", "/css/**", "/js/**", "/username", "/files/**", "/notifications", "/board/like/count").permitAll()
                        .requestMatchers("/profile/update").authenticated()
                        .requestMatchers("/mypage/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/")
                        .failureHandler(customAuthenticationFailureHandler())
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf.disable())
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(principalOauth2UserService)
                        )
                )
                .userDetailsService(customUserDetailsService)
                .build();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception.getMessage().equals("Account is locked")) {
                response.sendRedirect("/login?error=accountLocked");
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
