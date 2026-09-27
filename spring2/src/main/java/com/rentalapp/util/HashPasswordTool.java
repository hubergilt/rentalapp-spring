package com.rentalapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Standalone utility with its own main() - deliberately NOT a Spring
 * ApplicationRunner/CommandLineRunner.
 *
 * Spring Security builds its filter chain (and calls SecurityConfig's
 * userDetailsService() bean, which requires ADMIN_PASSWORD_HASH to already
 * be set) during application CONTEXT STARTUP - before any
 * ApplicationRunner gets to execute. That makes "boot the app to generate
 * the password the app needs to boot" impossible as a Spring bean. This
 * class only depends on spring-security-crypto (already on the classpath
 * via spring-boot-starter-security) and never starts a Spring context at
 * all, so it has nothing to be blocked by.
 *
 * Usage (see README "Setting the admin password"):
 *   mvn compile exec:java -Dexec.args="your-real-password"
 */
public final class HashPasswordTool {

    private HashPasswordTool() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            System.err.println("Usage: mvn compile exec:java -Dexec.args=\"your-real-password\"");
            System.exit(1);
        }
        String hash = new BCryptPasswordEncoder().encode(args[0]);
        System.out.println();
        System.out.println("ADMIN_PASSWORD_HASH=" + hash);
        System.out.println();
    }
}
