package com.rentalapp.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class BrandingModelAdvice {

    @Value("${app.name}")
    private String appName;

    @Value("${app.tagline}")
    private String appTagline;

    @Value("${app.theme}")
    private String theme;

    @ModelAttribute("appName")
    public String appName() { return appName; }

    @ModelAttribute("appTagline")
    public String appTagline() { return appTagline; }

    @ModelAttribute("theme")
    public String theme() { return theme; }

    /**
     * Plain SecurityContextHolder lookup, not the thymeleaf-extras-springsecurity6
     * dialect (sec:authentication) - that dialect has to be auto-registered by
     * Spring Boot's Thymeleaf autoconfiguration to work at all, and if it isn't
     * wired up for any reason, sec:* attributes silently do nothing instead of
     * erroring, which is exactly the kind of failure that's easy to miss. This
     * has no such dependency: it's just a direct read of the authenticated
     * principal already sitting in Spring Security's context for this request.
     */
    @ModelAttribute("username")
    public String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
    }
}
