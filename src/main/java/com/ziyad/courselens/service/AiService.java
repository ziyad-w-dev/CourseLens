package com.ziyad.courselens.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziyad.courselens.domain.dto.TopicAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
            
            Your task is to read the following Course Specification text and extract its topics.
            For EACH topic, you must generate 5 separate entries — one for each career track.
            
            ═══════════════════════════════════════════
            TRACKS (use these EXACT values):
            ═══════════════════════════════════════════
            - BACKEND_DEVELOPER
            - FRONTEND_DEVELOPER
            - DATA_ANALYST
            - AI_ML_ENGINEER
            - FULL_STACK
            
            ═══════════════════════════════════════════
            FOCUS LEVELS (use these EXACT values):
            ═══════════════════════════════════════════
            - MASTER_IT  → topic is critical for this track; deep mastery required
            - APPLY_IT   → topic is useful for this track; practical understanding required
            - KNOW_IT    → topic is general knowledge for this track; awareness is enough
            
            ═══════════════════════════════════════════
            RULES:
            ═══════════════════════════════════════════
            1. Extract topics ONLY from the "Course Content" section (usually labeled "C. Course Content").
            2. IGNORE headers, footers, page numbers, watermarks, and Arabic/decorative text.
            3. IGNORE sections like Learning Outcomes, Assessment Activities, References, Approval Data.
            4. courseTitle and courseCode come from the cover page or "Course Identification" section.
            5. For each topic in the Course Content section, output EXACTLY 5 entries (one per track above).
            6. Decide focusLevel based on how relevant the topic is to that track in real industry work.
            7. focusReason: 1-2 sentences explaining WHY this topic matters (or doesn't) for this specific track.
            8. realWorldExample: a concrete, practical example of using this topic in this track's daily work.
            9. focusReason and realWorldExample MUST be different for each track — tailored to that career.
            10. If lectureHours or labHours are missing for a topic, use 0.
            11. Return ONLY valid JSON. No markdown, no explanations, no code fences, no preamble.
            12. Topic title MUST be concise: max 100 characters. Use the EXACT topic name as written in the PDF — no extra description, no explanation, no subtitle.
            ═══════════════════════════════════════════
            OUTPUT FORMAT (JSON ARRAY):
            ═══════════════════════════════════════════
            [
              {
                "courseTitle": "string",
                "courseCode": "string",
                "title": "string (topic title)",
                "lectureHours": number,
                "labHours": number,
                "focusLevel": "MASTER_IT" | "APPLY_IT" | "KNOW_IT",
                "focusReason": "string",
                "realWorldExample": "string",
                "targetTrack": "BACKEND_DEVELOPER" | "FRONTEND_DEVELOPER" | "DATA_ANALYST" | "AI_ML_ENGINEER" | "FULL_STACK"
              }
            ]
            
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

    public List<TopicAiResponse> extractTopics(String pdfText) {
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

            // Parse #2 - turn the inner JSON string into a List of TopicAiResponse
            List<TopicAiResponse> topics = objectMapper.readValue(
                    innerJson,
                    new TypeReference<List<TopicAiResponse>>() {}
            );

            return topics;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
}