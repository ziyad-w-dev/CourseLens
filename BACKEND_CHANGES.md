# Backend changes for the frontend

Everything the React frontend (`frontend/`) needed from the backend that it didn't already provide.
No endpoints were added or removed, no fields were renamed, and every query is still scoped to the
current user's institution.

## Changed files

### `course/dto/CourseResponse.java`
- **Added** `List<String> startWith`.
- **Why:** the course card (screen 2a) has a **START WITH** box showing the first two MASTER IT topics.
  Only filled by `GET /api/courses` (student list); `null` elsewhere.

### `course/repository/TopicFocusRepository.java`
- **Added** `findTop2ByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(course, track, level)`.
- **Why:** returns the data for `startWith`: the first two MASTER_IT topics for the student's track, in
  syllabus order (topic id order). One query per course, the same as the existing count queries.

### `course/service/CourseService.java`
- `getCoursesForStudent()`: fills `startWith` from the new repository method, mapped to topic titles.
- `getCourseById()`:
  - sets `syllabusNumber` (1-based) on every topic, in the order of the course specification;
  - sets `masterCount` / `applyCount` / `knowCount` (counted from the topics already loaded, so no
    extra queries).
- **Why:** the course page (screen 3) shows `#05` next to each topic, plus the **4 MASTER IT · 3 APPLY IT ·
  2 KNOW IT** summary tiles. Before this change the counts were only sent on the course list.

### `course/dto/TopicResponse.java`
- **Added** `int syllabusNumber`.
- **Why:** the `#05` / `#09` numbers on the course page are the topic's position in the specification.
  The page lists topics grouped by focus level, so that position can't be read from the list order.

### `course/entity/Course.java`
- **Added** `@OrderBy("id ASC")` to `topics`.
- **Why:** topics are saved in the order Gemini reads them from the spec, so id order is syllabus order.
  Without `@OrderBy`, JPA doesn't promise any order, so `syllabusNumber` (and the "Next in MASTER IT"
  link and "1 of 9" study priority on the topic page, which use it) could come out wrong. No schema change.

### `auth/service/AuthService.java`
- **Changed** the message of the "unregistered domain" error from
  `"No registered institution for domain: <domain>"` to
  `"This email's university isn't registered on CourseLens yet."` Same exception and status (404).
- **Why:** screen 1c shows the backend's error message under the email field, and the design's copy is
  that sentence. The frontend tells this error apart by its status (404 = university not registered,
  409 = email already in use) and shows `message` as-is.

### `ai/service/AiService.java`
- **Added:** if the PDF opens but has no extractable text (a scanned / image-only spec), throw
  `InvalidFileException("File is not a readable PDF")` before calling Gemini.
- **Why:** screen 5d ("This file isn't a readable PDF"). Before this change a scanned PDF was sent to Gemini as an
  empty prompt and either failed as "AI busy" (wrong screen, 5c) or saved an empty course.

### `common/exception/GlobalExceptionHandler.java`
- **Added** a handler for `MaxUploadSizeExceededException` → **400** `"File is larger than 20 MB"`.
- **Why:** before this change an upload over 20 MB fell through to the catch-all → 500, which the professor screen
  shows as "The AI service is busy". It's a bad file, so it now returns 400 like the other file errors
  and gets screen 5d. The frontend also checks the size before uploading, so normally this never runs.

### `common/security/SecurityConfig.java`
- **Added** `exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))`.
- **Why:** with no auth entry point configured, Spring Security answers a missing / expired / invalid
  token with **403**, the same status as "logged in but not allowed". The frontend has to send the user
  to the "session expired" login screen (1d) on a **401**, so unauthenticated requests now return 401.
  Wrong-role requests (a student calling a DOCTOR endpoint) still return 403. Nothing is opened up: the
  same requests are rejected, only the status code changes.

## Error statuses the frontend relies on

| Screen | Request | Status → UI |
|---|---|---|
| 1c Sign up | `POST /api/auth/register` | 404 → university not registered (message under email), 409 → email in use (message under email), 400 → validation |
| 1b Log in | `POST /api/auth/login` | 401 → "Invalid email or password" (not treated as session expiry) |
| 1d Session expired | any authenticated request | 401 → clear session, go to `/login?expired=1` |
| 5c AI busy | `POST /api/courses/upload` | 503 (and any non-400 failure) → "The AI service is busy" |
| 5d Unreadable PDF | `POST /api/courses/upload` | 400 → "This file isn't a readable PDF" |

## Tests added / updated

- `course/service/CourseServiceTest` (new): `startWith` + counts on the student list, empty `startWith`
  when there are no MASTER topics, `syllabusNumber` + counts on the course page.
- `course/repository/TopicFocusRepositoryTest` (new, H2): the derived `findTop2…` query returns the first
  two MASTER_IT topics in id order, for the requested track only.
- `ai/service/AiServiceTest` (new): non-PDF bytes and a text-less PDF both throw `InvalidFileException`
  without calling Gemini.
- `common/security/SecurityConfigTest` (new, MockMvc): no token → 401, bad token → 401, student token on a
  DOCTOR endpoint → still 403.
- `common/exception/GlobalExceptionHandlerTest` (new): oversized upload → 400.
- `auth/service/AuthServiceTest` (updated): asserts the new unregistered-domain message.

`./mvnw test`: 19 tests, all passing.

---

# Follow-up: short topic titles and a single START WITH topic

## Changed files

### `course/service/CoursePersistenceService.java`
- **Added** a private `shortTitle(title)` helper, used when each `Topic` is built from the Gemini response.
  It cuts the title at the first `:`, `•` or `` (U+F0B7, the bullet some PDFs use), then trims.
  A title with none of those characters is kept as-is.
- **Why:** Gemini often returned the topic heading together with all its sub-points, e.g.
  `"Introduction to Databases: • Types of Databases • Basic Definitions ..."`, which made the topic
  lists and the START WITH box unreadable. Only the heading is stored now.
- Existing rows are **not** migrated; courses uploaded before this change keep their long titles until
  they are re-uploaded.

### `ai/service/CoursePromptBuilder.java`
- **Changed** the `title` instruction: the title must be only the topic heading, without its
  sub-points, bullets or anything after a colon (with an example).
- **Why:** fixes the long titles at the source. `shortTitle` stays as a safety net for when Gemini
  ignores the instruction.

### `course/dto/CourseResponse.java`
- **Changed** `startWith` from `List<String>` to `String`: the title of the **first** MASTER_IT topic
  for the student's track, in syllabus order. `null` when the course has no MASTER_IT topic
  (and, as before, `null` on every endpoint except `GET /api/courses`).
- **Why:** the START WITH box now shows a single topic. **Breaking for API clients:** the field is a
  string or `null` instead of an array; the frontend was updated in the same change.

### `course/repository/TopicFocusRepository.java`
- **Replaced** `findTop2ByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc` (returning a list)
  with `findFirstByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc` returning
  `Optional<TopicFocus>`.
- **Why:** only one topic is needed. It is ordered by topic id, which is the syllabus order (topics are
  saved in the order of the specification; it's the same order `syllabusNumber` is computed from).

### `course/service/CourseService.java`
- `getCoursesForStudent()`: sets `startWith` to the title from the new repository method, or `null`.

## Tests added / updated

- `course/service/CoursePersistenceServiceTest` (new): titles with `:`, `•` and U+F0B7 are cut to the
  heading; a title without any of them stays unchanged.
- `course/service/CourseServiceTest` (updated): `startWith` is the first MASTER_IT title; `null` when
  there are no MASTER_IT topics.
- `course/repository/TopicFocusRepositoryTest` (updated, H2): `findFirst…` returns the first MASTER_IT
  topic in syllabus order for the requested track, and empty for a track with none.

`./mvnw test`: 20 tests, all passing.

---

# Merge of `main` into `frontend`

`main` had 6 commits `frontend` was missing: SLF4J logging in `GlobalExceptionHandler`, logging of rejected
JWTs, a Gemini retry (up to 3 attempts), 409 on a duplicate course upload, a "topic heading only" prompt
instruction, and validation of the Gemini response. Both sides' behaviour is kept.

## Conflicts and how they were resolved

### `common/exception/GlobalExceptionHandler.java`
- **Kept from main:** `@Slf4j` logging instead of `printStackTrace()` (`log.warn` for expected Gemini
  failures, `log.error` with the stack trace for unexpected errors), the new
  `CourseAlreadyExistsException` → **409** handler, and the removal of the commented-out
  `InvalidTrackException` handler.
- **Kept from frontend:** the `MaxUploadSizeExceededException` → **400** `"File is larger than 20 MB"`
  handler.

### `ai/service/CoursePromptBuilder.java`
- Both branches had added their own "topic heading only" instruction. They are merged into **one**
  instruction: only the heading as written (max 100 characters), no sub-points / bullets, no trailing
  colon, with one example. `shortTitle()` in `CoursePersistenceService` still trims titles as a safety net.

## Auto-merged (checked by hand)

- `ai/service/AiService.java`: the scanned-PDF check (blank text → `InvalidFileException`, 400) runs
  before the prompt is built; main's 3-attempt retry and the new `CourseResponseValidator` (every track
  exactly once per course and per topic, titles ≤ 500 chars and unique) wrap the Gemini call. A response
  that fails validation counts as a failed attempt and is retried; after 3 failures → 503.
- `course/service/CourseService.java`: main's duplicate check
  (`existsCourseByCodeAndProgramAndInstitution`) → `CourseAlreadyExistsException` (409) sits next to
  frontend's single `startWith`, `syllabusNumber` and level counts. The check runs **after** the Gemini
  call, because the course code comes from the AI's reading of the PDF.
- `course/entity/Course.java`: main widened the unique constraint to `(institution_id, code, program)`;
  frontend's `@OrderBy("id ASC")` on `topics` is kept.
- `common/security/*`: main's JWT-rejection logging; frontend's 401 entry point in `SecurityConfig` is
  unchanged.

## Status the frontend relies on (new)

| Screen | Request | Status → UI |
|---|---|---|
| Professor upload | `POST /api/courses/upload` | 409 → "This course is already uploaded" (backend message shown), not "AI service is busy" |

## Tests added

- `course/service/CourseServiceTest`: a duplicate upload throws `CourseAlreadyExistsException` and
  nothing is saved.
- `common/exception/GlobalExceptionHandlerTest`: `CourseAlreadyExistsException` → 409 with its message.

`./mvnw test`: 22 tests, all passing.
