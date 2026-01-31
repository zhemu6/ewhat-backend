# AGENTS.md

Guidance for agentic coding assistants in this repo.

## Quick Facts

- Project: `ewhat-backend` (Spring Boot 3.5.6 parent)
- Java: 21 (`pom.xml`)
- Build: Maven Wrapper (`mvnw`, `mvnw.cmd`), Maven 3.9.11 (`.mvn/wrapper/maven-wrapper.properties`)
- DB/Cache: MySQL + Redis (configured via `application.yml` placeholders)
- Web: port `8123`, context path `/api` (`src/main/resources/application.yml`)
- Persistence: MyBatis-Plus + XML mappers (`src/main/resources/mapper/*.xml`)

## Commands

Use Maven Wrapper (Windows: `./mvnw.cmd`, macOS/Linux: `./mvnw`).

Build:

```bash
./mvnw clean package
./mvnw clean package -DskipTests
```

Run:

```bash
./mvnw spring-boot:run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Tests (JUnit via Spring Boot parent defaults; no explicit surefire config in `pom.xml`):

```bash
./mvnw test
./mvnw test -Dtest=EwhatBackendApplicationTests
./mvnw test "-Dtest=EwhatBackendApplicationTests#loadCanteenData"
```

Notes:
- PowerShell treats `#` as a comment: always quote `-Dtest=...#...`.
- If method selection is flaky (JUnit 5 + Surefire versions differ), run the class or use the IDE runner.
- No dedicated lint/format plugins (Checkstyle/Spotless/PMD/SpotBugs) found; treat `./mvnw test` / `./mvnw package` as the gate.

More useful Maven lifecycle targets:

```bash
./mvnw clean test
./mvnw clean package
./mvnw clean verify
```

Single test reminders:
- Class: `./mvnw test -Dtest=SomeTest`
- Method (typical): `./mvnw test "-Dtest=SomeTest#someMethod"`
- If running on Windows PowerShell, ALWAYS quote any arg containing `#`.

Local dev tips:
- Some tests are `@SpringBootTest` and may require MySQL/Redis running.
- Prefer writing tests that do not depend on external services unless explicitly intended.

## Configuration / Environment

- `src/main/resources/application.yml` uses `${...}` placeholders for secrets and connection details.
- `src/main/resources/application-dev.yml` contains local dev credentials and secrets; it is ignored via `.gitignore`.

Runtime defaults:
- Server: `8123`
- Context path: `/api`
- Profile: `dev` (set in `application.yml`)

Do not hardcode credentials into tracked files. Keep real values in env vars or untracked local config.

## Project Structure

Root package: `src/main/java/com/lushihao/ewhatbackend`

- `controller/admin/*Controller.java` and `controller/user/*Controller.java`
- `service/*Service.java` + `service/impl/*ServiceImpl.java`
- `mapper/*Mapper.java` + `src/main/resources/mapper/*Mapper.xml`
- `model/entity/*` (DB entities), `model/dto/*` (request DTO), `model/vo/*` (response VO)
- `config/*` (MVC/CORS/MyBatis-Plus/JWT/COS)
- `common/*` (response wrapper), `exception/*` (error model)

API routing conventions:
- Admin endpoints: `/admin/**`
- User endpoints: `/user/**`
- Health check: `/health`

Auth conventions:
- Interceptors: `src/main/java/com/lushihao/ewhatbackend/interceptor/JwtTokenAdminInterceptor.java`, `src/main/java/com/lushihao/ewhatbackend/interceptor/JwtTokenUserInterceptor.java`
- Registration: `src/main/java/com/lushihao/ewhatbackend/config/WebMvcConfiguration.java`

## Code Conventions (Observed)

Naming:
- Packages: lowercase; classes: `UpperCamelCase`; methods/fields: `lowerCamelCase`.
- Constants: `UPPER_SNAKE_CASE` (`constant/*`).

Lombok:
- Entities/DTO/VO commonly use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Controllers/services commonly use `@Slf4j`.

Imports:
- Avoid wildcard imports; remove unused imports (some existing files may not be clean).

Controllers:
- Use `@RestController` + `@RequestMapping` (often `/admin/...` or `/user/...`).
- Return `BaseResponse<T>` and build via `ResultUtils.success(...)`.
- Prefer throwing `BusinessException` / `ThrowUtils.throwIf(...)` instead of returning ad-hoc error payloads.

Services:
- Interface `XxxService`, implementation `XxxServiceImpl` (often extends `ServiceImpl<Mapper, Entity>`).

Persistence:
- Mapper interfaces + XML mappers; entities map to `tb_*` tables.
- Prefer `java.time.*` (e.g. `LocalDateTime`) for time fields.

MyBatis XML:
- Mapper namespace matches mapper interface (e.g. `com.lushihao.ewhatbackend.mapper.XxxMapper`).
- Keep SQL readable; avoid duplicating column lists when a `<sql id="...">` exists.

## Error Handling

- Error codes: `src/main/java/com/lushihao/ewhatbackend/exception/ErrorCode.java`.
- Throw business errors via `BusinessException` or `ThrowUtils.throwIf(...)`.
- Global mapping: `src/main/java/com/lushihao/ewhatbackend/exception/GlobalExceptionHandler.java` returns `BaseResponse<?>`.

Typical flow:
- Validate input in controller/service -> `ThrowUtils.throwIf(...)`.
- Throw `BusinessException` with `ErrorCode`.
- Let `GlobalExceptionHandler` convert exceptions to `BaseResponse`.

## Logging

- Use `@Slf4j` (`log.info`, `log.error`); avoid `System.out` / `printStackTrace`.

Log hygiene:
- Do not log secrets/tokens.
- Prefer structured placeholders: `log.info("msg: {}", value)`.

## Security (Hard Rules)

- Never commit secrets/tokens/keys.
- Treat secrets in code comments as leaked credentials; remove and rotate.
- When sharing snippets/logs, redact anything under `ewhat.*`, `cos.*`, `jwt.*`, `wechat.*`.

Known foot-guns:
- Never paste real values from `application-dev.yml` into issues/PRs.
- Treat any credential appearing in code comments as leaked.

## Tooling / Rules Files

- Cursor/Copilot rule files not found: no `.cursor/rules/*`, `.cursorrules`, `.github/copilot-instructions.md`.
- IDE notes:
  - `.vscode/settings.json`: `java.compile.nullAnalysis.mode=automatic`
  - `.idea/inspectionProfiles/Project_Default.xml`: JavadocDeclaration inspection; allows `@description`, `@createDate`
- Line endings: `.gitattributes` enforces `mvnw` as LF and `*.cmd` as CRLF.

Editing constraints:
- Preserve existing line endings; do not reformat the whole file unless asked.
- Avoid large mechanical refactors mixed with behavior changes.
