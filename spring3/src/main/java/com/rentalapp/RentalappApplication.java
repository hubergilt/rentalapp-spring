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
 * Jakarta EE 10 servlet container (Tomcat 11, JBoss EAP 8.1, WildFly 27+).
 *
 * Unlike the original Grails/Spring Boot 2 app -- which was pinned to
 * javax.servlet and therefore ONLY worked on Tomcat 9 / JBoss EAP 7.4 --
 * this app is built on Spring Boot 3 / jakarta.servlet, so the same WAR
 * file deploys unmodified to both Tomcat 11 and JBoss EAP 8.1.
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
