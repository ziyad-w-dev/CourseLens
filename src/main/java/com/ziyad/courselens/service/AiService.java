package com.ziyad.courselens.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ziyad.courselens.domain.dto.CourseAiResponse;
import com.ziyad.courselens.domain.dto.CourseFocusAiResponse;
import com.ziyad.courselens.domain.dto.TopicAiResponse;
import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.Track;
import com.ziyad.courselens.exception.AiUnavailableException;
import com.ziyad.courselens.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final int MAX_ATTEMPTS = 3;

    // Gemini generates for every track in the enum — the enum is the single source of truth.
    private static final List<Track> ACTIVE_TRACKS = List.of(Track.values());

    private static final Set<Track> EXPECTED_TRACKS = new HashSet<>(ACTIVE_TRACKS);

    private static final List<String> TRACK_NAMES = ACTIVE_TRACKS.stream()
            .map(Enum::name)
            .toList();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Strict parsing: a repeated key inside one object now throws instead of silently overwriting
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();

    private String buildPrompt(String pdfText, Program program) {
        // SOFTWARE_ENGINEERING -> "Software Engineering"
        String programName = java.util.Arrays.stream(program.name().split("_"))
                .map(w -> w.charAt(0) + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
        String n = String.valueOf(ACTIVE_TRACKS.size());
        String trackList = TRACK_NAMES.stream()
                .map(t -> "        - " + t)
                .collect(Collectors.joining("\n"));

        return """
        You are an expert curriculum designer for a personalized learning platform.
        
        The students reading your output are enrolled in the {PROGRAM} program.
        
        Your task is to read the following Course Specification text and extract:
        (1) programRelevance: why this course matters for a {PROGRAM} student in general
        (2) course-level personalization for {N} career tracks
        (3) per-topic personalization for the same {N} tracks
        
        ═══════════════════════════════════════════
        PROGRAM RELEVANCE
        ═══════════════════════════════════════════
        programRelevance: 2-3 sentences on why THIS COURSE matters for ANY {PROGRAM} student,
        regardless of career track — its place in the degree and what it enables later.
        Always encouraging. Do not mention specific career tracks here.
        
        ═══════════════════════════════════════════
        TRACKS (use these EXACT values):
        ═══════════════════════════════════════════
{TRACKS}
        
        ═══════════════════════════════════════════
        TOPIC-LEVEL FOCUS LEVELS (use these EXACT values):
        ═══════════════════════════════════════════
        - MASTER_IT  → topic is critical for this track; deep mastery required
        - APPLY_IT   → topic is useful for this track; practical understanding required
        - KNOW_IT    → topic is general knowledge for this track; awareness is enough
        
        ═══════════════════════════════════════════
        COURSE-LEVEL PERSONALIZATION
        ═══════════════════════════════════════════
        In addition to per-topic focus, generate {N} course-level entries — one per track.
        Course-level entries have:
        - focusReason: 1-2 sentences on why THIS ENTIRE COURSE matters for this track,
          even if some topics are less central. Always frame it positively — every course
          contributes to a developer's growth. Never suggest skipping or de-prioritizing
          the course as a whole.
        - realWorldExample: a concrete career scenario where someone in this track
          applies what they learned from this entire course.
        
        Course-level entries do NOT have a focusLevel — only topic-level entries do.
        
        ═══════════════════════════════════════════
        RULES:
        ═══════════════════════════════════════════
        1. Extract topics ONLY from the "Course Content" section (usually labeled "C. Course Content").
        2. IGNORE headers, footers, page numbers, watermarks, and Arabic/decorative text.
        3. IGNORE sections like Learning Outcomes, Assessment Activities, References, Approval Data.
        4. courseTitle and courseCode come from the cover page or "Course Identification" section.
            courseLevel is the LEVEL number from "Level/year at which this course is offered"
            (e.g. "5th Level / 3rd Year" → 5). Use the level, NOT the year. If not found, use 0.
        5. For each topic in the Course Content section, output EXACTLY {N} topic entries (one per track).
        6. Output EXACTLY {N} courseFocus entries (one per track), each a SEPARATE JSON object.
        7. Every topic entry and every courseFocus entry MUST include targetTrack.
        8. Decide focusLevel based on how relevant the topic is to that track in real industry work.
        9. focusReason: 1-2 sentences explaining WHY this topic matters (or doesn't) for this specific track.
        10. realWorldExample: a concrete, practical example of using this topic in this track's daily work.
        11. focusReason and realWorldExample MUST be different for each track — tailored to that career.
        12. If lectureHours or labHours are missing for a topic, use 0.
        13. Topic title MUST be concise: max 100 characters. Use the EXACT topic name as written in the PDF — no extra description, no explanation, no subtitle, and no trailing punctuation such as ":".
        14. Course-level focusReason must NEVER use language like "skip", "ignore", "not relevant",
                "low priority", or imply the course is unimportant for any track. Always be encouraging.
        
        ═══════════════════════════════════════════
        PDF TEXT:
        ═══════════════════════════════════════════
        %s
        """
                .replace("{N}", n)
                .replace("{TRACKS}", trackList)
                .replace("{PROGRAM}", programName)
                .formatted(pdfText);
    }

    // Forces Gemini to generate inside this shape: required keys can't go missing,
    // enum values can't be misspelled, and two objects can't merge into one.
    private Map<String, Object> buildResponseSchema() {
        Map<String, Object> str = Map.of("type", "STRING");
        Map<String, Object> integer = Map.of("type", "INTEGER");
        Map<String, Object> trackEnum = Map.of("type", "STRING", "enum", TRACK_NAMES);
        Map<String, Object> focusEnum = Map.of("type", "STRING", "enum", List.of(
                "MASTER_IT", "APPLY_IT", "KNOW_IT"));

        Map<String, Object> courseFocusItem = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "targetTrack", trackEnum,
                        "focusReason", str,
                        "realWorldExample", str),
                "required", List.of("targetTrack", "focusReason", "realWorldExample"),
                "propertyOrdering", List.of("targetTrack", "focusReason", "realWorldExample"));

        Map<String, Object> topicItem = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "title", str,
                        "targetTrack", trackEnum,
                        "lectureHours", integer,
                        "labHours", integer,
                        "focusLevel", focusEnum,
                        "focusReason", str,
                        "realWorldExample", str),
                "required", List.of("title", "targetTrack", "lectureHours", "labHours",
                        "focusLevel", "focusReason", "realWorldExample"),
                "propertyOrdering", List.of("title", "targetTrack", "lectureHours", "labHours",
                        "focusLevel", "focusReason", "realWorldExample"));

        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "courseTitle", str,
                        "courseCode", str,
                        "courseLevel", integer,
                        "programRelevance", str,
                        "courseFocus", Map.of("type", "ARRAY", "items", courseFocusItem),
                        "topics", Map.of("type", "ARRAY", "items", topicItem)),
                "required", List.of("courseTitle", "courseCode", "courseLevel", "programRelevance", "courseFocus", "topics"),
                "propertyOrdering", List.of("courseTitle", "courseCode", "courseLevel", "programRelevance", "courseFocus", "topics"));
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));

        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.3,
                "responseMimeType", "application/json",
                "responseSchema", buildResponseSchema(),
                // Structured extraction doesn't need "thinking" — turning it off makes the call faster
                "thinkingConfig", Map.of("thinkingBudget", 0)
        );

        return Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );
    }

    public CourseAiResponse extractCourseData(String pdfText, Program program) {
        // Build the request once — it's identical on every attempt
        String prompt = buildPrompt(pdfText, program);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey); // key in a header, so it never shows up in error messages or logs
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(buildRequestBody(prompt), headers);

        String fullUrl = apiUrl;

        Exception lastError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                long start = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
                System.out.println("Gemini attempt " + attempt + " took "
                        + (System.currentTimeMillis() - start) / 1000.0 + "s");

                // Parse #1 - Gemini's envelope → navigable tree
                JsonNode root = objectMapper.readTree(response.getBody());

                // dig inward: candidates → [0] → content → parts → [0] → text
                String innerJson = root
                        .path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();
                System.out.println("RAW GEMINI: " + innerJson);

                // Parse #2 - inner JSON string → ONE CourseAiResponse object
                CourseAiResponse courseData = objectMapper.readValue(innerJson, CourseAiResponse.class);

                // Parsing only proves the JSON is well-formed; this proves the data is complete
                if (isValid(courseData)) {
                    return courseData;
                }
                lastError = new IllegalStateException("Gemini returned incomplete or duplicated tracks");

            } catch (HttpServerErrorException | JsonProcessingException e) {
                // 5xx (e.g. 503 overloaded) or broken JSON: temporary, worth retrying
                lastError = e;
            }

            System.out.println("Gemini attempt " + attempt + " failed: " + lastError.getMessage());

            if (attempt < MAX_ATTEMPTS) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new AiUnavailableException("Gemini failed after " + MAX_ATTEMPTS + " attempts: "
                + lastError.getMessage(), lastError);
    }

    private boolean isValid(CourseAiResponse ai) {
        int n = ACTIVE_TRACKS.size();

        if (ai.getCourseFocus() == null || ai.getTopics() == null || ai.getTopics().isEmpty()) {
            return false;
        }
        if (ai.getProgramRelevance() == null || ai.getProgramRelevance().isBlank()) {
            return false;
        }

        // Course level: exactly n entries, each active track exactly once
        Set<Track> courseTracks = ai.getCourseFocus().stream()
                .map(CourseFocusAiResponse::getTargetTrack)
                .collect(Collectors.toSet());
        if (ai.getCourseFocus().size() != n || !courseTracks.equals(EXPECTED_TRACKS)) {
            return false;
        }

        // Topic level: every entry needs a title and a focus level
        if (ai.getTopics().stream().anyMatch(t -> t.getTitle() == null || t.getFocusLevel() == null)) {
            return false;
        }

        // ...and each topic has exactly n entries, each active track exactly once
        Map<String, List<TopicAiResponse>> byTitle = ai.getTopics().stream()
                .collect(Collectors.groupingBy(TopicAiResponse::getTitle));

        for (List<TopicAiResponse> group : byTitle.values()) {
            Set<Track> topicTracks = group.stream()
                    .map(TopicAiResponse::getTargetTrack)
                    .collect(Collectors.toSet());
            if (group.size() != n || !topicTracks.equals(EXPECTED_TRACKS)) {
                return false;
            }
        }
        return true;
    }

    public CourseAiResponse courseAiAnalyze(MultipartFile file, Program program) {
        String pdfText;

        // Step 1 - extract PDF text (try-with-resources auto-closes the document)
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfText = stripper.getText(document);
        } catch (IOException e) {
            throw new InvalidFileException("File is not a valid PDF");
        }

        // Step 2 - send PDF to Gemini (OUTSIDE the try — not a file problem)
        return extractCourseData(pdfText, program);
    }
}