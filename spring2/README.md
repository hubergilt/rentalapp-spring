# rentalapp (legacy-container build: Tomcat 9 / JBoss EAP 7.4)

A plain Spring Boot 2.7 / javax-EE admin-CRUD dashboard over the
**existing** `rentaldb` database (`tenants`, `rooms`, `tenancies`,
`rent_payments`, `security_deposits`, plus the `current_tenancies` view) -
targeting **Tomcat 9** and **JBoss EAP 7.4** specifically.

**This is the legacy-container sibling of the jakarta.* rentalapp build**
(Spring Boot 3, targeting Tomcat 11 / JBoss EAP 8.1). Same domain model,
same screens, same business rules, same Thymeleaf templates - built as a
**separate codebase**, not a build flag, because Spring Boot 3 requires
Spring Framework 6, which is `jakarta.*` throughout (not just at the
servlet-API layer): `javax.persistence.Entity` and
`jakarta.persistence.Entity` are different classes in different packages,
so a WAR compiled against one cannot run on a container that only knows
the other. There is no config toggle that reconciles this.

**This app never creates or alters the schema.** Exactly like the
jakarta build, `rentaldb`'s tables are assumed to be owned and migrated
elsewhere - `spring.jpa.hibernate.ddl-auto` is permanently pinned to
`none`, and every entity maps onto tables that already exist.

## What's different from the jakarta.* build

Only what the namespace split actually forces - everything else (domain
model, screens, business rules, Thymeleaf templates, static assets, the
`.env` loader, the dashboard) is identical:

| Concern | jakarta build | This (legacy) build |
|---|---|---|
| Spring Boot | 3.3.x | **2.7.18** (last 2.7.x release) |
| Namespace | `jakarta.persistence.*`, `jakarta.validation.*` | **`javax.persistence.*`, `javax.validation.*`** |
| Hibernate | 6.x | **5.6.x** (same `@Subselect`/`@Immutable`/`@Synchronize` API for `current_tenancies` - `org.hibernate.annotations` didn't move) |
| Spring Security | 6.x | **5.7.x** (same `SecurityFilterChain` bean-style config, but one method-level difference - see below) |
| MySQL driver artifact | `com.mysql:mysql-connector-j` | **`mysql:mysql-connector-java`** (pre-rename groupId/artifactId) |
| Java target | 17 | **11** (works down to 8 too - nothing here uses syntax newer than that; see the comment in `pom.xml`) |
| Target containers | Tomcat 11, JBoss EAP 8.1, WildFly 27+ | **Tomcat 9, JBoss EAP 7.4** |

**The Spring Security method-level difference:** `SecurityConfig.java` uses
`.authorizeRequests()`/`.antMatchers(String...)` here instead of the
jakarta build's `.authorizeHttpRequests()`/`.requestMatchers(String...)`.
The plain-String-varargs `requestMatchers(String...)` convenience overload
was only added in Spring Security 5.8 (shipped with Spring Boot 3.0) -
5.7.x (what Boot 2.7.18 carries) only has
`requestMatchers(RequestMatcher...)` and `requestMatchers(HttpMethod,
String...)`, neither of which accepts a plain list of path patterns.
`antMatchers` is soft-deprecated in 5.7 in favor of an API surface that
isn't fully there yet, but it's fully supported until Spring Security 6.0
actually removes it - correct and safe to use here.

Everything under "Schema fidelity," "What's on each screen," and the
dashboard's stat/recent-activity logic carries over unchanged from the
jakarta build's README - refer to that document for the domain-level
detail; this one only covers what's specific to deploying on the older
containers.

## 1. Prerequisites

- Java 11+ (or 8 - see the note in `pom.xml`)
- Maven 3.6+
- A running MySQL 8 instance with the `rentaldb` schema already migrated -
  this app assumes the tables already exist.
- Only for WAR deployment: Tomcat 9 and/or JBoss EAP 7.4 installed locally
  or on a server you control.

## 2. Configure your environment

```
cp .env.example .env
```

Fill in `.env` - same variables, same loading order
(`$CATALINA_BASE/.env`, then `$JBOSS_HOME/standalone/.env`, then `./.env`
for local runs) as the jakarta build. Real OS environment variables always
take priority over `.env` file values, and a `.env` line with a value left
blank (`DB_URL=` with nothing after the `=`) is treated as unset rather
than "set to empty" - this bit us once during the jakarta build's own
rollout (a blank `DB_URL=` silently broke the datasource), so the fix is
carried over here too.

### Setting the admin password

Same as the jakarta build - a plain, Spring-free utility, not a Spring
`ApplicationRunner` (Spring Security builds its filter chain during
context *startup*, before any runner bean gets a turn, so trying to boot
the app to generate the password it needs to boot is a dead end):

```
mvn compile exec:java -Dexec.args="your-real-password"
```

Copy the printed `$2a$...` string into `.env` as `ADMIN_PASSWORD_HASH`.
`make pass PASSWD='...'` runs the same thing.

### Creating rentalapp's own database user (least privilege)

```sql
CREATE USER 'rentalapp'@'localhost' IDENTIFIED BY 'changeme';
GRANT SELECT, INSERT, UPDATE, DELETE ON rentaldb.* TO 'rentalapp'@'localhost';
FLUSH PRIVILEGES;
```

## 3. Run it locally

```
mvn spring-boot:run
```

Then open <http://localhost:8080> - login screen, then the dashboard.

## 4. Building the WAR

```
mvn clean package
```

Produces `target/rentalapp.war` (same `finalName` pinning as the jakarta
build, so it deploys at context path `/rentalapp`).

## 5. Deploying to Tomcat 9

Identical workflow to the jakarta build's Tomcat 11 instructions - only
the install path differs:

```
sudo -u tomcat cp .env /opt/tomcat/9.0.x/.env
sudo chmod 600 /opt/tomcat/9.0.x/.env
make deploy-tomcat TOMCAT_HOME=/opt/tomcat/9.0.x
```

Restart Tomcat, then visit `http://localhost:8080/rentalapp/`.
`make undeploy-tomcat` / `make redeploy-tomcat` work the same way.

## 6. Deploying to JBoss EAP 7.4

Same marker-file deployment model as JBoss EAP 8.1 - drop the WAR into
`standalone/deployments/`, the running server picks it up live:

```
sudo -u jboss cp .env /opt/jboss-eap-7.4/standalone/.env
sudo chmod 600 /opt/jboss-eap-7.4/standalone/.env
make deploy-jboss JBOSS_HOME=/opt/jboss-eap-7.4
```

Watch `standalone/log/server.log` for `... deployed "rentalapp.war"`.

### The `WEB-INF/jboss-deployment-structure.xml` fix still applies here

EAP 7.4's WildFly-core module system has the exact same default behavior
that broke deployment on EAP 8.1 during this app's development: it
auto-injects its own `org.slf4j` module (backed by
`slf4j-jboss-logmanager`) into every deployment, which collides with the
SLF4J + Logback jars this WAR bundles transitively via
`spring-boot-starter-logging`, and Spring Boot's `LogbackLoggingSystem`
fails hard at startup with:

```
java.lang.IllegalArgumentException: LoggerFactory is not a Logback
LoggerContext but Logback is on the classpath. Either remove Logback or
the competing implementation (class org.slf4j.impl.Slf4jLoggerFactory ...)
```

`src/main/webapp/WEB-INF/jboss-deployment-structure.xml` is carried over
from the jakarta build unchanged and fixes this the same way: it tells
WildFly's module system to keep its default logging modules out of this
deployment's classloader. **Tomcat ignores this file entirely**, so it's
safe in the one WAR that targets both containers.

### A note on the MySQL driver in JBoss EAP 7.4

Same as the jakarta build's JBoss note: the driver is bundled in
`WEB-INF/lib` via the `mysql:mysql-connector-java` Maven dependency, so no
JBoss-level module or `standalone.xml` `<datasource>` is needed - the app
manages its own Hikari pool from `application.yml`.

## 7. Tests

```
mvn test
```

In-memory H2 (`test` Spring profile), never touches the real `rentaldb`.

## 8. Keeping the two builds in sync

Any future change to business logic, screens, or the dashboard needs to be
made in **both** codebases - they don't share source. Porting a change
from the jakarta build to this one is usually just
`jakarta.persistence` -> `javax.persistence`,
`jakarta.validation` -> `javax.validation` - but check for these two
version traps every time, since both bit this port during development:

- **Java syntax newer than 11**: text blocks (`"""`), `Stream.toList()`
  (Java 16+ - use `.collect(Collectors.toList())` instead), and anything
  else that assumes a newer language/API level than this build's Java 11
  target.
- **Spring Security's `requestMatchers(String...)`**: only exists from
  Spring Security 5.8 (Boot 3.0) onward. This build's Spring Security
  5.7.x needs `.authorizeRequests()`/`.antMatchers(String...)` instead -
  see the note in `SecurityConfig.java`.

Everything else (repositories, `WebConfig`, Thymeleaf templates, static
assets) uses Spring Framework APIs whose package names and method surfaces
didn't move between the two generations, so it copies over verbatim.
