# student-management
# Student Management Backend

A REST API for managing students, secured with **JWT authentication**. Built for the Full Stack Java Programming Lab (FSJP) – Assignment 2.

## Features
- User registration and login with **BCrypt** password hashing
- **JWT** token generation and a custom JWT authentication filter
- Only `/api/auth/register` and `/api/auth/login` are public; everything else needs a valid token
- Student CRUD (create, list, get by id, update, delete)
- Bean validation (required fields, email format, year 1–4)
- Duplicate email protection (users and students) → `409 Conflict`
- JSON error responses for 400 / 401 / 403 / 404 / 409 / 500, including **expired** and **invalid** JWTs

## Technology Stack
Java 21 · Spring Boot 3.3.5 · Spring Web · Spring Security 6 · Spring Data JPA (Hibernate) · MySQL 8 · JJWT 0.12.6 · Maven · Postman

## Prerequisites
- JDK 21
- Maven 3.9+ (or use your IDE's built-in Maven)
- MySQL 8 + MySQL Workbench
- Postman

## Project Structure
```
src/main/java/com/example/studentmanagement
├── StudentManagementApplication.java
├── config        (SecurityConfig, ApplicationConfig)
├── controller    (AuthController, StudentController)
├── dto           (request/response records, ErrorResponse)
├── entity        (User, Student)
├── exception     (custom exceptions + GlobalExceptionHandler)
├── repository    (UserRepository, StudentRepository)
├── security      (JwtService, JwtAuthenticationFilter, entry point, ...)
└── service       (AuthService, StudentService)
```

## MySQL Setup (MySQL Workbench)
1. Open MySQL Workbench and connect to your local server.
2. Open a new SQL tab, paste the contents of [`sql/create_database.sql`](sql/create_database.sql) and click the ⚡ **Execute** button.
   (Or just run: `CREATE DATABASE student_management;`)
3. Refresh the **Schemas** panel – you should see `student_management`.

The tables (`users`, `students`) are created automatically by Hibernate on first run (`spring.jpa.hibernate.ddl-auto=update`).

## Configuration (no secrets in code)
The app reads these **environment variables** (see `.env.example`):

| Variable | Required | Meaning |
|---|---|---|
| `DB_PASSWORD` | yes | Your MySQL password |
| `JWT_SECRET` | yes | Base64 string, at least 32 bytes |
| `DB_USERNAME` | no (default `root`) | MySQL user |
| `DB_URL` | no | Defaults to `jdbc:mysql://localhost:3306/student_management...` |
| `JWT_EXPIRATION_MS` | no (default `3600000` = 1 hour) | Token lifetime |

Generate a JWT secret:
```bash
openssl rand -base64 32
```
(No openssl on Windows? Use Git Bash, or PowerShell: `[Convert]::ToBase64String((1..32 | % { Get-Random -Max 256 }))`)

**Set them (pick one):**

*Windows PowerShell* (current terminal only):
```powershell
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="paste_generated_secret_here"
```
*macOS / Linux:*
```bash
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET="paste_generated_secret_here"
```
*IntelliJ:* Run → Edit Configurations → select the app → **Environment variables** → add them.

> Spring Boot does not read `.env` files by itself, so don't just create one and expect it to work.

## Run the Application
```bash
mvn clean package          # builds and runs tests (tests use in-memory H2, no MySQL needed)
mvn spring-boot:run        # starts on http://localhost:8080
```
Or run `StudentManagementApplication` from your IDE.

If startup fails with `Could not resolve placeholder 'DB_PASSWORD'` or `'JWT_SECRET'`, the env var isn't set in the terminal/IDE you launched from.

## API Documentation

| Method | URL | Auth | Description | Success |
|---|---|---|---|---|
| POST | `/api/auth/register` | public | Register a user | 201 |
| POST | `/api/auth/login` | public | Log in, returns JWT | 200 |
| POST | `/api/students` | Bearer | Add a student | 201 |
| GET | `/api/students` | Bearer | List all students | 200 |
| GET | `/api/students/{id}` | Bearer | Get one student | 200 |
| PUT | `/api/students/{id}` | Bearer | Update a student | 200 |
| DELETE | `/api/students/{id}` | Bearer | Delete a student | 200 |

Error format:
```json
{
  "timestamp": "2026-10-17T12:00:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/students",
  "validationErrors": { "email": "Email format is invalid" }
}
```

## Postman Testing
Quick way: import `postman/Student-Management.postman_collection.json` and run the requests in order (it saves the token and student id automatically).

Manual steps (`baseUrl` = `http://localhost:8080`):

**1. Register** — `POST /api/auth/register`, Body → raw → JSON
```json
{ "name": "Rishi", "email": "rishi@example.com", "password": "Password@123" }
```
Expected **201 Created**:
```json
{ "id": 1, "name": "Rishi", "email": "rishi@example.com" }
```
Registering the same email again → **409 Conflict**.

**2. Login** — `POST /api/auth/login`
```json
{ "email": "rishi@example.com", "password": "Password@123" }
```
Expected **200 OK**:
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer", "expiresInMs": 3600000, "email": "rishi@example.com" }
```
Wrong password → **401** `Invalid email or password`.

**3. Copy the token** (the value of `token`, without quotes).

**4. Use it:** in each protected request go to **Authorization** tab → Type **Bearer Token** → paste the token. (This sends `Authorization: Bearer <token>`.)

**5. Add student** — `POST /api/students`
```json
{ "name": "Aarav Sharma", "email": "aarav@example.com", "branch": "IT", "year": 2 }
```
Expected **201 Created**:
```json
{ "id": 1, "name": "Aarav Sharma", "email": "aarav@example.com", "branch": "IT", "year": 2 }
```

**6. View all** — `GET /api/students` → **200 OK**, JSON array of students.

**7. View by ID** — `GET /api/students/1` → **200 OK**, one student. Unknown id → **404**.

**8. Update** — `PUT /api/students/1`
```json
{ "name": "Aarav Sharma", "email": "aarav@example.com", "branch": "Computer Engineering", "year": 3 }
```
Expected **200 OK** with the updated student.

**9. Delete** — `DELETE /api/students/1` → **200 OK**
```json
{ "message": "Student deleted successfully" }
```

**10. Security check** — `GET /api/students` with **No Auth** (or a fake token) → **401 Unauthorized**:
```json
{ "status": 401, "error": "Unauthorized", "message": "Authentication required. Send a valid 'Authorization: Bearer <token>' header.", "path": "/api/students" }
```
Fake/tampered token → **401** `Invalid JWT token.` · Expired token → **401** `JWT token has expired. Please log in again.`

(Validation failure on any POST/PUT, e.g. invalid email or year 9 → **400** with `validationErrors`.)

## Run the Tests
```bash
mvn test
```
`StudentApiIntegrationTest` runs the whole flow (register → login → CRUD → 401 checks) against in-memory H2.

## GitHub Upload Commands
```bash
cd student-management
git init
git add .
git commit -m "Student Management backend with Spring Boot, JWT and MySQL"
git branch -M main
# create an EMPTY repo on github.com first, then:
git remote add origin https://github.com/<your-username>/student-management.git
git push -u origin main
```
`.gitignore` already excludes `target/` and `.env`. Never commit your real password or JWT secret.

## Troubleshooting
| Problem | Fix |
|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | Set the env var in the same terminal/IDE run config |
| `WeakKeyException` / Base64 error at startup | Secret must be Base64 and ≥ 32 bytes: `openssl rand -base64 32` |
| `Access denied for user 'root'` | Wrong `DB_USERNAME` / `DB_PASSWORD` |
| `Unknown database 'student_management'` | Run the SQL script first |
| Port 8080 in use | Add `server.port=8081` to `application.properties` |
