# FSJP Assignment 2 – Requirement Checklist

| # | Requirement | Where |
|---|---|---|
| 1 | Java 21, Spring Boot 3.x, Web, Security, JPA, MySQL, JWT, Maven | `pom.xml` |
| 2 | POST /api/auth/register, POST /api/auth/login | `AuthController` |
| 2 | BCrypt hashing, no plain-text passwords | `ApplicationConfig`, `AuthService` |
| 2 | JWT generation after login | `JwtService`, `AuthService.login` |
| 2 | JWT authentication filter | `JwtAuthenticationFilter` |
| 2 | Spring Security config; only register/login public | `SecurityConfig` |
| 2 | Expired / invalid JWT handled (401 JSON) | `JwtAuthenticationFilter`, `SecurityErrorWriter` |
| 2 | Duplicate registration by email blocked (409) | `AuthService`, `GlobalExceptionHandler` |
| 3 | 5 student CRUD endpoints | `StudentController` |
| 3 | Student fields id/name/email/branch/year | `Student` entity |
| 3 | Entities, repos, services, controllers, DTOs, validation | respective packages |
| 4 | DB `student_management`, JPA/Hibernate, application.properties | `application.properties`, `sql/create_database.sql` |
| 4 | Credentials/secrets via env vars | `application.properties`, `.env.example` |
| 4 | Workbench + local config explained | `README.md` |
| 5 | Packages config/controller/dto/entity/repository/service/security/exception | `src/main/java/...` |
| 6 | Validation, status codes, not-found, duplicate, JSON errors, 401 for unauthenticated | `GlobalExceptionHandler`, DTOs, `JwtAuthenticationEntryPoint` |
| 7 | Postman instructions (all 10 steps) | `README.md`, `postman/*.json` |
| 8 | README + GitHub commands + SQL script | `README.md`, `sql/` |
| 9 | Compiled / run verification | **NOT verified here** – run `mvn clean package` yourself |
