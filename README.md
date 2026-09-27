# rentalapp

A plain Java admin-CRUD dashboard over the existing `rentaldb` database
(`tenants`, `rooms`, `tenancies`, `rent_payments`, `security_deposits`,
plus the `current_tenancies` view) - a from-scratch, no-Grails rewrite of
[rentalapp-grails](https://github.com/hubergilt/rentalapp-grails).

The project was rewritten twice to target different Java EE container
generations. Both builds share the same domain model, screens, and business
rules, but use different namespace generations:

## Builds

### `spring2` (legacy-container)
**Spring Boot 2.7.x** | **javax.* namespace** | **Tomcat 9** | **JBoss EAP 7.4**
- Uses `javax.persistence`, `javax.validation`, Hibernate 5, Spring Security 5.7
- Targets the pre-Jakarta EE 9 container generation
- Last 2.7.x release before the Jakarta namespace migration
- `pom.xml` targets Java 11 (also works with Java 8)

### `spring3` (jakarta rewrite)
**Spring Boot 3.3.x** | **jakarta.* namespace** | **Tomcat 11** | **JBoss EAP 8.1**
- Uses `jakarta.persistence`, `jakarta.validation`, Hibernate 6, Spring Security 6.x
- Targets the Jakarta EE 9+ container generation (Servlet 6.0, EE 10+)
- Built directly on Spring Boot 3, no Grails/GORM dependency

Both builds produce a WAR (`target/rentalapp.war`) deployable to their
respective containers. They share the same Thymeleaf templates, business
logic, and dashboard, but **cannot share a single codebase** because
`javax.persistence.Entity` and `jakarta.persistence.Entity` are different
classes in different packages.

## Quick Links

- [spring2 README »](spring2/README.md) — legacy-container build details, Tomcat 9/JBoss EAP 7.4 deployment
- [spring3 README »](spring3/README.md) — Jakarta EE build details, Tomcat 11/JBoss EAP 8.1 deployment

## Core Principles

- **Schema never created or altered** — `spring.jpa.hibernate.ddl-auto` is
  permanently pinned to `none`; all tables are assumed to already exist in
  `rentaldb` (migrated elsewhere, e.g. Flyway).
- **Form-based admin login** with BCrypt-hashed password from `.env` env
  var — no user table, identical to the original Grails app.
- **MySQL 8** is the configured dialect; other databases require adding the
  driver dependency and an extra Spring profile.
- **War packaging** via Maven — `mvn clean package` produces
  `target/rentalapp.war`.

## Prerequisites

- **Java**: 11 for spring2 / 17 for spring3
- **Maven**: 3.6+ (spring2) / 3.9+ (spring3)
- **MySQL 8** instance with `rentaldb` schema already migrated
- Container-specific requirements documented in each subproject's README

## Developing Changes

Any change to business logic, screens, or Thymeleaf templates must be
ported to **both** codebases independently. The namespace difference means:

| jakarta build (`spring3`) | legacy build (`spring2`) |
|---|---|
| `jakarta.persistence.*` | `javax.persistence.*` |
| `jakarta.validation.*` | `javax.validation.*` |
| `.authorizeHttpRequests()` / `.requestMatchers()` | `.authorizeRequests()` / `.antMatchers()` |
| Java 17+ syntax | Java 11 target |

See each subproject's README for detailed deployment instructions,
environment configuration, and container-specific notes.