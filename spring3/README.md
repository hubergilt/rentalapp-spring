# rentalapp (plain Java / Jakarta EE rewrite)

A plain Spring Boot 3 / Jakarta EE admin-CRUD dashboard over the **existing**
`rentaldb` database (`tenants`, `rooms`, `tenancies`, `rent_payments`,
`security_deposits`, plus the `current_tenancies` view) - a from-scratch,
no-Grails rewrite of [rentalapp-grails](https://github.com/hubergilt/rentalapp-grails),
built specifically so the same WAR deploys unmodified to **both Tomcat 11**
and **JBoss EAP 8.1**.

**This app never creates or alters the schema.** Exactly like the original,
`rentaldb`'s tables are assumed to be owned and migrated elsewhere (e.g. by
Flyway, in a separate project) - `spring.jpa.hibernate.ddl-auto` is
permanently pinned to `none` in `application.yml`, and every entity maps
onto tables that already exist.

## Why a rewrite, and why this stack

The original app was pinned to Grails 6.2.x / Spring Boot 2.7, which is
built on `javax.servlet.*` (pre-Jakarta EE 9). That's what made it only
deployable to Tomcat 9 and JBoss EAP 7.4 - Tomcat 10/11, JBoss EAP 8.x, and
current WildFly all moved to the `jakarta.*` namespace, which is a hard
break, not a config toggle.

This rewrite drops Grails and GORM entirely and is built directly on:

| Concern | Technology |
|---|---|
| Language / runtime | Java 17, plain Spring Boot 3.3 (`jakarta.*`) |
| Web / views | Spring MVC + Thymeleaf (server-rendered HTML, no SPA/JS framework) |
| Persistence | Spring Data JPA / Hibernate 6, mapped onto the live schema (`ddl-auto: none`) |
| Auth | Spring Security, single form-based admin login, BCrypt hash from an env var - no user table, exactly like the original |
| Packaging | Maven, `war` packaging, deployable standalone (`spring-boot:run`) or as a WAR into an external container |

Because it's Jakarta EE 9+ throughout, the same WAR works on:

| Container | Servlet namespace | Compatible? |
|---|---|---|
| Tomcat 9.x | `javax.*` | :x: No (this rewrite only - use the original Grails app for Tomcat 9) |
| **Tomcat 11** | `jakarta.*` | :white_check_mark: Yes |
| JBoss EAP 7.4 | `javax.*` | :x: No |
| **JBoss EAP 8.1** | `jakarta.*` (Jakarta EE 10) | :white_check_mark: Yes |
| WildFly 27+ | `jakarta.*` (Jakarta EE 10) | :white_check_mark: Yes (untested, but same namespace as JBoss EAP 8.x) |

## Project layout

```
rentalapp/
├── pom.xml                          Maven build - spring-boot-starter-parent 3.3.x, war packaging
├── Makefile                         war / deploy-tomcat / deploy-jboss / undeploy-* / redeploy-* targets
├── .env.example                     copy to .env, fill in real values
├── src/main/java/com/rentalapp/
│   ├── RentalappApplication.java    SpringBootServletInitializer - runs standalone OR as a WAR
│   ├── config/
│   │   ├── DotenvEnvironmentPostProcessor.java   loads .env (CATALINA_BASE, JBOSS_HOME, or cwd)
│   │   ├── SecurityConfig.java      single admin user, BCrypt, form login
│   │   ├── WebConfig.java           String->entity converters for <select> form binding
│   │   └── (no password-hashing bean here - see util/HashPasswordTool.java below)
│   ├── domain/                      Tenant, Room, Tenancy, RentPayment, SecurityDeposit,
│   │                                 CurrentTenancy (read-only, @Subselect over the view)
│   ├── repository/                  Spring Data JPA repositories
│   ├── service/                     TenantService, TenancyService - business rules that
│   │                                 mirror the DB's CHECK/ON DELETE RESTRICT constraints
│   ├── controller/                  one controller per screen: DashboardController ('/'),
│   │                                 plus the original's custom Tenant/Tenancy controllers
│   │                                 and scaffolded-equivalent Room/RentPayment/
│   │                                 SecurityDeposit/CurrentTenancy ones
│   ├── util/
│   │   └── HashPasswordTool.java    `mvn compile exec:java -Dexec.args="..."` - plain
│   │                                 main(), deliberately outside the Spring context
│   └── web/                         cross-cutting: branding model attributes, 404 handling
└── src/main/resources/
    ├── application.yml              datasource (env-var driven), ddl-auto: none, branding
    ├── templates/                   Thymeleaf views: layout, login, one folder per entity,
    │                                 plus fragments/deposit-panel.html (AJAX installments)
    └── static/{css,js}/             theme stylesheet, deposits.js (fetch()-based panel)
└── src/main/webapp/WEB-INF/
    └── jboss-deployment-structure.xml   JBoss/WildFly-only logging fix (Tomcat ignores it) -
                                          see "A note on logging in JBoss" below
```

## 1. Prerequisites

- Java 17+
- Maven 3.9+ (or generate a wrapper yourself: `mvn -N io.takari:maven:wrapper`)
- A running MySQL 8 instance with the `rentaldb` schema already migrated -
  **this app assumes the tables already exist**, same as the original.
- Only for WAR deployment: Tomcat 11 and/or JBoss EAP 8.1 installed locally
  or on a server you control.

## 2. Configure your environment

```
cp .env.example .env
```

Fill in `.env` - see the comments in `.env.example` for what each variable
does. As with the original, `.env` is never bundled into the WAR (it's
gitignored) and is loaded from a container-specific path at deploy time
(see "Deploying" below) - not from inside the WAR itself.

### Setting the admin password

Only a **BCrypt hash** is ever configured, never the plaintext. Generating
it is a plain, Spring-free utility (`HashPasswordTool`) that never boots the
application context - it can't be a Spring `ApplicationRunner`/
`CommandLineRunner`, because Spring Security builds its filter chain (and
calls `SecurityConfig.userDetailsService()`, which requires
`ADMIN_PASSWORD_HASH` to already be set) *during context startup*, before
any runner bean would get a chance to execute:

```
mvn compile exec:java -Dexec.args="your-real-password"
```

Copy the printed `$2a$...` string into `.env` as `ADMIN_PASSWORD_HASH`.
`make pass PASSWD='...'` runs the same thing.

### Creating rentalapp's own database user (least privilege)

Same intent as the original - grant only `SELECT`/`INSERT`/`UPDATE`/`DELETE`,
never schema-modifying privileges:

```sql
CREATE USER 'rentalapp'@'localhost' IDENTIFIED BY 'changeme';
GRANT SELECT, INSERT, UPDATE, DELETE ON rentaldb.* TO 'rentalapp'@'localhost';
FLUSH PRIVILEGES;
```

## 3. Run it locally

```
mvn spring-boot:run
```

Then open <http://localhost:8080> - you'll land on the login screen, and
from there the dashboard (stat cards, recent activity, quick links to
every screen).

## 4. What's on each screen

Functionally equivalent to the original:

| Screen | Behavior |
|---|---|
| **Dashboard** | Landing page after login (`/`) - stat cards for tenants, room occupancy breakdown, active/ended tenancies, and this month's collected rent; a recent-activity feed (latest rent payments, newest tenancies); quick links to every other screen. The original Grails app didn't have one of these - it landed straight on whatever the first scaffolded screen was - so this is a genuine addition, built off the same repositories every other screen uses. |
| **Tenants** | Search box (name / national ID), full list/show/create/edit; delete blocked with a friendly message if the tenant still has tenancies or rent payments. |
| **Rooms** | Plain CRUD - list/show/create/edit/delete. |
| **Tenancies** | Filter by status (active/ended), room, tenant. The show page has an **inline, AJAX-refreshed security deposit installments panel** (`fragments/deposit-panel.html` + `static/js/deposits.js`) - add or remove an installment without a full page reload. Delete is blocked if installments still exist. |
| **Rent Payments** | Append-only ledger, sortable by deposit date. |
| **Security Deposits** | Same ledger, browsable independent of a specific tenancy. |
| **Current Occupancy** | Read-only over the `current_tenancies` view - only index/show exist, no create/edit/delete route anywhere. One row per room (including vacant ones), matching the view's `LEFT JOIN` semantics. |

## 5. Schema fidelity

Every entity here was checked directly against the `mysqldump` schema you
provided, not guessed - the same details the original called out still
apply:

- `tenants` has three name parts (`first_names`, `paternal_surname`,
  `maternal_surname`) and no email/phone columns.
- `rooms`' label column is `name`, plus a separate `floor`.
- `rent_payments.tenant_id`/`room_id` are genuinely nullable - kept
  optional in `RentPayment`, not forced required.
- `rent_payments.remarks` is required; `security_deposits.remarks` is
  optional.
- `current_tenancies` is a `LEFT JOIN` from `rooms` - one row per room,
  including vacant ones. `CurrentTenancy.isOccupied()` and the views render
  `— vacant —` accordingly.
- The three `CHECK` constraints on `tenancies` (end date, deposit refund
  amount, deposit refund date) are re-validated in `TenancyService` so the
  user sees a friendly message instead of a raw SQL error.

If `rentaldb` has since diverged (a new migration changed a column), fix it
the same way as before: update the relevant entity's `@Column` mapping to
match, then watch for `SQLSyntaxErrorException: Unknown column` in the
logs on startup.

## 6. Building the WAR

```
mvn clean package
```

Produces `target/rentalapp.war` (the `finalName` in `pom.xml` is pinned to
`rentalapp`, so it always deploys at context path `/rentalapp` regardless
of the project's Maven version).

## 7. Deploying to Tomcat 11

One-time server setup (same idea as the original, dedicated service
account):

```
sudo groupadd --system tomcat
sudo useradd --system --gid tomcat --no-create-home --shell /usr/sbin/nologin tomcat
sudo usermod -aG tomcat "$USER"
sudo chown -R tomcat:tomcat /opt/tomcat/11.0.2
sudo chmod 2775 /opt/tomcat/11.0.2/webapps
```

Create the server's own `.env` (never bundled in the WAR):

```
sudo -u tomcat cp .env /opt/tomcat/11.0.2/.env
sudo chmod 600 /opt/tomcat/11.0.2/.env
```

Then:

```
make deploy-tomcat TOMCAT_HOME=/opt/tomcat/11.0.2
```

Restart Tomcat, then visit `http://localhost:8080/rentalapp/`.

`make undeploy-tomcat` / `make redeploy-tomcat` are also provided.

## 8. Deploying to JBoss EAP 8.1

JBoss/WildFly use marker-file-based deployment scanning instead of a
webapps-copy-and-restart model - dropping the WAR into
`standalone/deployments/` is enough; the running server picks it up live
(no restart required, unlike Tomcat).

One-time setup - put `.env` where `DotenvEnvironmentPostProcessor` looks
for it under JBoss (`$JBOSS_HOME/standalone/.env`):

```
sudo -u jboss cp .env /opt/jboss-eap-8.1/standalone/.env
sudo chmod 600 /opt/jboss-eap-8.1/standalone/.env
```

Then:

```
make deploy-jboss JBOSS_HOME=/opt/jboss-eap-8.1
```

Watch `standalone/log/server.log` for a line ending in `... deployed
"rentalapp.war"`. Once deployed, visit `http://localhost:8080/rentalapp/`.

`make undeploy-jboss` / `make redeploy-jboss` are also provided.

### A note on the MySQL driver in JBoss

This WAR bundles its own `mysql-connector-j` inside `WEB-INF/lib` (a normal
Spring Boot WAR dependency), so it does **not** need a JBoss-level module
or a `<datasource>` defined in `standalone.xml` - the app manages its own
Hikari connection pool from `application.yml`, exactly as it does under
Tomcat. There's nothing container-specific to configure on either side
beyond dropping the WAR in place.

### A note on logging in JBoss (`WEB-INF/jboss-deployment-structure.xml`)

WildFly's module system auto-injects its own `org.slf4j` module (backed by
`slf4j-jboss-logmanager`) into every deployment by default. This WAR also
bundles its own SLF4J + Logback (transitively, via
`spring-boot-starter-logging`) - two SLF4J bindings on one classpath means
Spring Boot's `LogbackLoggingSystem` fails hard at startup with:

```
java.lang.IllegalArgumentException: LoggerFactory is not a Logback
LoggerContext but Logback is on the classpath. Either remove Logback or
the competing implementation (class org.slf4j.impl.Slf4jLoggerFactory ...)
```

`src/main/webapp/WEB-INF/jboss-deployment-structure.xml` fixes this by
telling WildFly's module system to keep its default logging modules out of
this one deployment's classloader, so only the jars actually bundled in
the WAR are used. **Tomcat has no concept of this file and ignores it
entirely** - it's purely additive, which is why the same WAR still works
on both containers without a build-time branch.

## 9. Tests

```
mvn test
```

Uses an in-memory H2 database (`test` Spring profile in `application.yml`,
`ddl-auto: create-drop`) - never touches the real `rentaldb`.

## 10. Known gaps vs. the original (by design, for a first pass)

- Only MySQL is wired up in `application.yml` (the uploaded schema dump was
  MySQL-specific). Adding PostgreSQL/Oracle/SQL Server back is a matter of
  adding the driver dependency to `pom.xml` and an extra Spring profile in
  `application.yml` with that engine's URL/dialect - the entity mappings
  themselves are portable.
- No i18n message bundle yet (the original had `messages.properties`) -
  all UI text is hardcoded English in the Thymeleaf templates.
- No automated test suite has been written yet beyond the H2 profile
  wiring - `mvn test` runs green with zero tests today.
