package com.ziyad.courselens.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.gemini.*;
import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.exception.AiUnavailableException;
import com.ziyad.courselens.common.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final RestClient restClient;
    private final CoursePromptBuilder promptBuilder;
    private final CourseSchemaBuilder schemaBuilder;
    private final ObjectMapper objectMapper;
    private final CourseResponseValidator courseResponseValidator;

    private String extractCourseDetails(MultipartFile file) {
        try(PDDocument document = Loader.loadPDF(file.getBytes())){
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("Extracted {} characters from {} pages",text.length(),document.getNumberOfPages());
            log.debug("Extracted PDF text:\n{}", text);
            return text;
        }catch (IOException e){
            log.warn("Failed to read uploaded PDF {}",file.getOriginalFilename(), e);
            throw new  InvalidFileException("File is not a valid PDF");
        }
    }

    public CourseAiResponse courseAiAnalyze(MultipartFile file, Program program) {
        String pdfText = extractCourseDetails(file);
        String prompt = promptBuilder.build(pdfText, program);
        log.debug("prompt sent to Gemini\n{}",prompt);
        Part part = new Part(prompt);
        Content content = new Content(List.of(part));

        ThinkingConfig thinkingConfig = new ThinkingConfig(0);
        GenerationConfig generationConfig = new GenerationConfig("application/json", 0.3
                ,thinkingConfig,schemaBuilder.build());

        GeminiRequest geminiRequest = new GeminiRequest(List.of(content),generationConfig);

        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiOnce(geminiRequest);          // success → leave the method

            } catch (HttpServerErrorException | AiUnavailableException e) {
                log.warn("Gemini attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());

                if (attempt == maxAttempts) {                          /
                    throw new AiUnavailableException("Gemini failed after " + maxAttempts + " attempts", e);
                }

                sleep(attempt * 2000L);
            }
        }
        throw new AiUnavailableException("Gemini retry loop ended unexpectedly");
    }

    private CourseAiResponse callGeminiOnce(GeminiRequest request){
        GeminiResponse response = restClient.post()
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);
        log.debug("Gemini response{}", response);

        List<Candidate> candidates = response.candidates();
        Candidate first = candidates.get(0);
        Content firstContent = first.content();
        List<Part> parts = firstContent.parts();
        Part firstPart = parts.get(0);
        String text = firstPart.text();
        log.info("Gemini response text: {}", text);

        try {
            CourseAiResponse aiResponse = objectMapper.readValue(text, CourseAiResponse.class);
            courseResponseValidator.validate(aiResponse);
            return aiResponse;
        }catch (JsonProcessingException e){
            log.warn("Failed to parse Gemini response into CourseAiResponse. Raw text: {}", text, e);
            throw new AiUnavailableException("Gemini returned invalid course JSON", e);
        }
    }
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiUnavailableException("Interrupted while waiting to retry Gemini", e);
        }
    }
}