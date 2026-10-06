package com.rentalapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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

    /**
     * Actuator endpoints (used by Spring Boot Admin) are polled by a server, not a person, so
     * they can't use the form login. This chain matches only /actuator/** and takes precedence
     * over the form-login chain below. It accepts HTTP Basic with the SAME single admin user
     * defined above (no second account), keeps no session, and needs no CSRF token.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
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
