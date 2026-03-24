package com.example.doanck.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC (không cần login)
                        // =========================
                        .requestMatchers(
                                "/",
                                "/movies",
                                "/movies/**",
                                "/login",
                                "/register",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // =========================
                        // ADMIN
                        // =========================
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // =========================
                        // USER (cần login)
                        // =========================
                        .requestMatchers(
                                "/seats/**",
                                "/payment/**",
                                "/tickets",
                                "/checkin/**"
                        ).authenticated()

                        .anyRequest().permitAll()
                )

                .userDetailsService(customUserDetailsService)

                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/movies", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/movies")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}