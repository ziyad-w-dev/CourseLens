# CourseLens

![CI](https://github.com/ziyad-w-dev/CourseLens/actions/workflows/ci.yml/badge.svg)

**AI-powered personalized study guides for university students.**

A doctor uploads a course specification PDF. CourseLens extracts the course and its topics with Gemini 2.5 Flash and builds a guide for every career track: how deep each topic should go for that track, why it matters, and a real-world example of it in use. A backend student and a data analyst open the same course and each get a guide written for them.

> **Status:** The backend is complete and works end to end. It runs in Docker against a cloud MySQL database, and every push is built and tested automatically with GitHub Actions. Deployment and the frontend are next.

---

## How it works

```
Doctor uploads course PDF
        │
        ▼
PDFBox extracts text ──► Gemini 2.5 Flash (structured JSON output)
                                   │
                                   ▼
                     Validate: every track present exactly once
                     (retry up to 3× on failure)
                                   │
                                   ▼
          Course ─┬─ CourseFocus  (1 per track: why this course matters)
                  └─ Topic ── TopicFocus (1 per track: MASTER_IT / APPLY_IT / KNOW_IT)
                                   │
                                   ▼
        Student sees the courses in their program, personalized to their track
```

**Tracks:** `BACKEND_DEVELOPER` · `FRONTEND_DEVELOPER` · `DATA_ANALYST`

**Programs:** Software Engineering · Computer Science · Data Analytics · Computer Engineering · Artificial Intelligence. The student's program is passed to Gemini, so explanations are framed for that degree.

---

## Tech stack

| Layer | Tech |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.5 |
| Persistence | MySQL, Spring Data JPA / Hibernate |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Validation | Jakarta Bean Validation |
| Mapping | MapStruct |
| AI | Gemini 2.5 Flash (REST, structured output) |
| PDF parsing | Apache PDFBox 3 |
| Testing | JUnit 5, Mockito, H2 (in-memory, for the app-context test) |
| Build | Maven |
| Containers | Docker |
| CI | GitHub Actions (build + tests on every push) |

---

## Features

### Multi-tenant by institution
- Each user belongs to an `Institution`, resolved from their **email domain** at registration. Unregistered domains are rejected.
- The `institutionId` is signed into the JWT.
- Every course query is scoped to the caller's institution (`findByIdAndInstitution`, `findByProgramAndInstitution`, …), so one university can never read another's data.

### Authentication & authorization
- Register and login with BCrypt-hashed passwords and stateless JWT auth. Emails are trimmed and lowercased.
- The JWT subject is the **user ID**, not the email, so tokens stay valid if an email changes.
- Registration always creates a `STUDENT` (the role defaults in the entity and isn't part of the request), so nobody can sign themselves up as a doctor. Doctor accounts are currently promoted directly in the database.
- Role checks happen in two places: `SecurityConfig` URL rules and method-level `@PreAuthorize`.

### AI pipeline
- **Structured output:** a JSON response schema is sent with every request, so Gemini can't drop required fields or invent enum values.
- **Completeness validation:** valid JSON isn't enough. Every course and every topic must have exactly one entry per track, or the response is rejected.
- **Retries:** up to 3 attempts on 5xx errors, broken JSON, or incomplete data, then a clean `503` for the client.
- **Strict parsing:** duplicate JSON keys throw an error instead of silently overwriting each other.
- **Two-step parsing:** `readTree` walks Gemini's envelope, then `readValue` maps the inner payload to typed DTOs.
- The API key goes in a request header, not the URL, so it never appears in logs or error messages.
- The track list comes from the `Track` enum, which feeds the prompt, the schema, and the validation. Adding a track takes one line.

### Error handling
A `@RestControllerAdvice` returns one consistent `ErrorResponse` shape for every error:

| Case | Status |
|---|---|
| Validation failure / malformed body / invalid PDF | 400 |
| Invalid credentials | 401 |
| Access denied | 403 |
| Resource not found / unknown institution | 404 |
| Email already registered | 409 |
| AI unavailable after retries | 503 |
| Anything else | 500 (generic message, no internals leaked) |

---

## API

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a student account |
| POST | `/api/auth/login` | Public | Get a JWT |
| POST | `/api/courses/upload` | DOCTOR | Upload a course spec PDF (multipart, `file`, max 20 MB) |
| GET | `/api/courses/my` | DOCTOR | Courses I uploaded |
| GET | `/api/courses` | STUDENT | Courses in my program, with Master / Apply / Know topic counts for my track |
| GET | `/api/courses/{id}` | STUDENT | Full course guide personalized to my track |

---

## Design decisions

- **Normalized `Topic` / `TopicFocus`.** Topic facts (title, hours) are stored once, and the per-track focus lives in its own table. A single wide table made it unclear what a quiz should attach to.
- **Topics get a focus level, courses don't.** Topics are ranked `MASTER_IT` / `APPLY_IT` / `KNOW_IT`. Course-level guidance is encouraging prose with no ranking, because a ranked course reads as "you can skip this."
- **Tenant comes from the token, never the request.** The institution is read from the server-signed JWT and the logged-in user, never from anything the client sends.
- **AI call outside the transaction.** The slow Gemini call happens first. Only the save runs inside `@Transactional` (`CoursePersistenceService`), so no DB transaction stays open while the app waits on the network.
- **MapStruct over ModelMapper.** Mappers are generated at compile time: type-safe, no reflection, and mistakes show up as build errors.
- **Gemini over Groq.** Chosen for output quality on long, structured responses.
- **Secrets only in environment variables.** The DB credentials, JWT secret, and API key are never committed.
- **Tests never touch real infrastructure.** CI runs against an in-memory H2 database with fake secrets, so tests are fast, repeatable, and can't leak or damage real data.

---

## Running locally

**Requirements:** Java 17, MySQL, a Gemini API key.

1. Create a MySQL database.
2. Set environment variables:
   ```
   DB_URL=jdbc:mysql://localhost:3306/courselens
   DB_USERNAME=...
   DB_PASSWORD=...
   JWT_SECRET=...        # at least 32 characters (HS256)
   GEMINI_API_KEY=...
   ```
3. Run:
   ```
   ./mvnw spring-boot:run
   ```
4. Hibernate creates the tables on first run. Then add an institution before registering, because registration rejects email domains with no matching institution:
   ```sql
   INSERT INTO institutions (name, domain) VALUES ('University of Hail', 'uoh.edu.sa');
   ```
5. To test doctor endpoints, register a user, then promote them:
   ```sql
   UPDATE users SET role = 'DOCTOR', track = NULL WHERE email = 'doctor@uoh.edu.sa';
   ```

The API runs on `http://localhost:8080`. CORS is open to `localhost:5173` and `localhost:3000` for frontend development.

### Or run with Docker

1. Create a `.env` file in the project root with the same variables as above (no quotes around values). It's already in `.gitignore`, so it's never committed.
   > If your MySQL runs on your own machine, use `host.docker.internal` instead of `localhost` in `DB_URL`. Inside a container, `localhost` means the container itself.
2. Build and run:
   ```
   ./mvnw clean package -DskipTests
   docker build -t courselens .
   docker run -p 8080:8080 --env-file .env courselens
   ```

---

## Roadmap

- [x] Docker image
- [x] CI with GitHub Actions
- [ ] Deployment (Oracle Cloud, with CD from GitHub Actions)
- [ ] Integration tests
- [ ] Structured logging (replace `printStackTrace` / `System.out`)
- [ ] Frontend (React)
- [ ] Doctor sign-up request → admin approval flow
- [ ] Institution onboarding

---

## Author

**Ziyad Al-Qasim**, Software Engineering student at the University of Hail
- LinkedIn: www.linkedin.com/in/ziyad-alqassim-4956b1395