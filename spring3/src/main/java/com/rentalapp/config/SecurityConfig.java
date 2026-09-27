package com.rentalapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A simple, dependency-free form-based login gates the whole app, same as
 * the original - there is no GORM/JPA-backed user table, since auth
 * doesn't belong in the Flyway-owned schema. Exactly one admin account,
 * configured entirely from environment variables (ADMIN_USERNAME /
 * ADMIN_PASSWORD_HASH). Only a BCrypt hash is ever configured - never a
 * plaintext password.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.admin-username}")
    private String adminUsername;

    @Value("${app.admin-password-hash}")
    private String adminPasswordHash;

    /**
     * Plain BCrypt, not Spring Security's {id}-prefixed DelegatingPasswordEncoder -
     * ADMIN_PASSWORD_HASH is stored as a raw "$2a$..." hash (see util/HashPasswordTool /
     * README "Setting the admin password"), same format as the original app.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        if (adminPasswordHash == null || adminPasswordHash.isBlank()) {
            throw new IllegalStateException(
                "ADMIN_PASSWORD_HASH is not set. Generate one (see README 'Setting the admin " +
                "password') and put it in your .env file or environment before starting the app.");
        }
        User.UserBuilder builder = User.withUsername(adminUsername)
                .password(adminPasswordHash)
                .roles("ADMIN");
        return new InMemoryUserDetailsManager(builder.build());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/static/**", "/webjars/**", "/css/**", "/js/**", "/img/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
