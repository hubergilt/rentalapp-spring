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
    @SuppressWarnings("deprecation")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // .authorizeRequests()/.antMatchers(String...) rather than
        // .authorizeHttpRequests()/.requestMatchers(String...): the latter's
        // plain-String-varargs convenience overload was only added in Spring
        // Security 5.8 (shipped with Spring Boot 3.0). Boot 2.7.18 carries
        // Spring Security 5.7.x, one version short of it -
        // requestMatchers(String...) doesn't compile there (only
        // requestMatchers(RequestMatcher...) and requestMatchers(HttpMethod,
        // String...) exist). antMatchers is soft-deprecated in 5.7 (in favor of
        // the API that isn't fully available yet) but fully supported until
        // Spring Security 6.0 removes it - safe and correct for this version.
        http
            .authorizeRequests(auth -> auth
                .antMatchers("/static/**", "/webjars/**", "/css/**", "/js/**", "/img/**").permitAll()
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
