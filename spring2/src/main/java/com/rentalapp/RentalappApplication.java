package com.rentalapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point.
 *
 * Runs standalone with an embedded container (`mvn spring-boot:run`,
 * or `java -jar rentalapp.war`) OR is deployed as a WAR into an external
 * javax.servlet 4.0 (Java EE 8-era) container - Tomcat 9 or JBoss EAP 7.4.
 *
 * This is the LEGACY-CONTAINER SIBLING of the jakarta-namespace rentalapp
 * build (Spring Boot 3, Tomcat 11 / JBoss EAP 8.1). The two are separate
 * codebases, not one build with a flag: Spring Boot 3 requires Spring
 * Framework 6, which is jakarta.* throughout (not just at the servlet-API
 * layer) - javax.persistence.Entity and jakarta.persistence.Entity are
 * different classes in different packages, so a WAR compiled against one
 * cannot run on a container that only knows the other. This build is
 * pinned to Spring Boot 2.7.x / Hibernate 5.x / Spring Security 5.7.x,
 * all of which are javax.*, matching what Tomcat 9 and JBoss EAP 7.4
 * actually provide.
 */
@SpringBootApplication
public class RentalappApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(RentalappApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(RentalappApplication.class, args);
    }
}
