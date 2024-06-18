package com.example.demo;

import com.example.demo.entity.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.demo.repository")
@EnableWebSocketMessageBroker
public class DemoApplication {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        logger.info("DemoApplication has started");
    }

    @Bean
    public CommandLineRunner createAdminUser(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByNickname("admin").isEmpty()) {
                Users admin = new Users();
                admin.setEmail("admin@peppermarket.com");
                admin.setPassword(passwordEncoder.encode("1234")); // 원하는 비밀번호로 설정하세요.
                admin.setNickname("admin");
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
                logger.info("Admin user created: admin@peppermarket.com");
            }
        };
    }
}
