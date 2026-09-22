package com.ziyad.courselens.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziyad.courselens.domain.dto.CourseAiResponse;
import com.ziyad.courselens.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String buildPrompt(String pdfText) {
        return """
        You are an expert curriculum designer for a personalized learning platform.
        
        Your task is to read the following Course Specification text and extract:
        (1) course-level personalization for 5 career tracks
        (2) per-topic personalization for the same 5 tracks
        
        ═══════════════════════════════════════════
        TRACKS (use these EXACT values):
        ═══════════════════════════════════════════
        - BACKEND_DEVELOPER
        - FRONTEND_DEVELOPER
        - DATA_ANALYST
        - AI_ML_ENGINEER
        - FULL_STACK
        
        ═══════════════════════════════════════════
        TOPIC-LEVEL FOCUS LEVELS (use these EXACT values):
        ═══════════════════════════════════════════
        - MASTER_IT  → topic is critical for this track; deep mastery required
        - APPLY_IT   → topic is useful for this track; practical understanding required
        - KNOW_IT    → topic is general knowledge for this track; awareness is enough
        
        ═══════════════════════════════════════════
        COURSE-LEVEL PERSONALIZATION
        ═══════════════════════════════════════════
        In addition to per-topic focus, generate 5 course-level entries — one per track.
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
        5. For each topic in the Course Content section, output EXACTLY 5 topic entries (one per track).
        6. Output EXACTLY 5 courseFocus entries (one per track).
        7. Decide focusLevel based on how relevant the topic is to that track in real industry work.
        8. focusReason: 1-2 sentences explaining WHY this topic matters (or doesn't) for this specific track.
        9. realWorldExample: a concrete, practical example of using this topic in this track's daily work.
        10. focusReason and realWorldExample MUST be different for each track — tailored to that career.
        11. If lectureHours or labHours are missing for a topic, use 0.
        12. Return ONLY valid JSON. No markdown, no explanations, no code fences, no preamble.
        13. Topic title MUST be concise: max 100 characters. Use the EXACT topic name as written in the PDF — no extra description, no explanation, no subtitle.
        14. Course-level focusReason must NEVER use language like "skip", "ignore", "not relevant",
            "low priority", or imply the course is unimportant for any track. Always be encouraging.
        
        ═══════════════════════════════════════════
        OUTPUT FORMAT (JSON OBJECT):
        ═══════════════════════════════════════════
        {
          "courseTitle": "string",
          "courseCode": "string",
          "courseFocus": [
            {
              "targetTrack": "BACKEND_DEVELOPER" | "FRONTEND_DEVELOPER" | "DATA_ANALYST" | "AI_ML_ENGINEER" | "FULL_STACK",
              "focusReason": "string",
              "realWorldExample": "string"
            }
          ],
          "topics": [
            {
              "title": "string (topic title)",
              "lectureHours": number,
              "labHours": number,
              "focusLevel": "MASTER_IT" | "APPLY_IT" | "KNOW_IT",
              "focusReason": "string",
              "realWorldExample": "string",
              "targetTrack": "BACKEND_DEVELOPER" | "FRONTEND_DEVELOPER" | "DATA_ANALYST" | "AI_ML_ENGINEER" | "FULL_STACK"
            }
          ]
        }
        
        ═══════════════════════════════════════════
        PDF TEXT:
        ═══════════════════════════════════════════
        %s
        """.formatted(pdfText);
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));

        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.3,
                "responseMimeType", "application/json"
        );

        return Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );
    }

    public CourseAiResponse extractCourseData(String pdfText) {
        try {
            // Step 1 - build the prompt
            String prompt = buildPrompt(pdfText);

            // Step 2 - build the request body
            Map<String, Object> requestBody = buildRequestBody(prompt);

            // Step 3 - set headers (Gemini requires JSON content type)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Step 4 - wrap body + headers into an HttpEntity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Step 5 - Gemini wants the API key as a query param: ?key=YOUR_KEY
            String fullUrl = apiUrl + "?key=" + apiKey;

            // Step 6 - send the POST request
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            // Step 7
            // Parse #1 - turn Gemini's raw response into a navigable tree
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

            // Parse #2 - turn the inner JSON string into ONE CourseAiResponse object
            CourseAiResponse courseData = objectMapper.readValue(
                    innerJson,
                    CourseAiResponse.class
            );

            return courseData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    public CourseAiResponse courseAiAnalyze(MultipartFile file) {
        String pdfText;

        // Step 1 - extract PDF text (try-with-resources auto-closes the document)
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfText = stripper.getText(document);
        } catch (IOException e) {
            throw new InvalidFileException("File is not a valid PDF");
        }

        // Step 2 - send PDF to Gemini (OUTSIDE the try — not a file problem)
        return extractCourseData(pdfText);
    }
}