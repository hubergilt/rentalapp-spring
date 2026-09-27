package com.rentalapp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small, explicit .env loader -- deliberately not a third-party
 * auto-detecting library, so what got loaded (and from where) is always
 * printed to stdout at startup.
 *
 * Where it looks, in order, stopping at the first file found:
 *   1. $CATALINA_BASE/.env               (WAR deployed into Tomcat)
 *   2. $JBOSS_HOME/standalone/.env       (WAR deployed into JBoss EAP / WildFly)
 *   3. ./.env                            (running from the project root:
 *                                          `mvn spring-boot:run`, `java -jar`)
 *
 * Real OS environment variables always take priority over anything read
 * from a .env file here -- this only fills in values that are not already
 * set, via a low-priority PropertySource appended at the end of the list.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = locateEnvFile();
        if (envFile == null) {
            System.out.println("[dotenv] no .env file found (checked CATALINA_BASE, JBOSS_HOME/standalone, and cwd) - relying on real environment variables only");
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                // strip matching surrounding quotes, if any
                if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                        || value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (value.isBlank()) {
                    // A line like `DB_URL=` with nothing after the `=` almost always means
                    // "I'm not using this one" (e.g. a template placeholder left untouched),
                    // not "set this to the empty string". Skipping it matters because Spring's
                    // ${DB_URL:default-value} placeholder syntax only falls back to the default
                    // when the property is ABSENT - a property that resolves to "" is treated as
                    // present-and-empty, so the fallback never kicks in and downstream config
                    // (here, the whole JDBC URL) silently resolves to nothing instead of the
                    // intended default. Loading blank values as if they were unset avoids that trap.
                    System.out.println("[dotenv] skipping " + key + " - present but blank in " + envFile.getFileName());
                    continue;
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            System.out.println("[dotenv] found " + envFile + " but could not read it: " + e.getMessage());
            return;
        }

        environment.getPropertySources().addLast(new MapPropertySource("dotenvFile", values));
        System.out.println("[dotenv] loaded " + values.size() + " value(s) from " + envFile.toAbsolutePath());
    }

    private Path locateEnvFile() {
        String catalinaBase = firstNonBlank(System.getProperty("catalina.base"), System.getenv("CATALINA_BASE"));
        if (catalinaBase != null) {
            Path candidate = Path.of(catalinaBase, ".env");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        String jbossHome = System.getenv("JBOSS_HOME");
        if (jbossHome != null) {
            Path candidate = Path.of(jbossHome, "standalone", ".env");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        Path cwd = Path.of(".env");
        if (Files.isRegularFile(cwd)) {
            return cwd;
        }

        return null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
