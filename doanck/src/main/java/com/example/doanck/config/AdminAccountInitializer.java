package com.example.doanck.config;

import com.example.doanck.model.User;
import com.example.doanck.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountInitializer {

    @Bean
    public CommandLineRunner initAdminAccount(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByUsername("admin");

            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@local");
            }

            admin.setRole("ADMIN");
            admin.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(admin);
        };
    }
}
