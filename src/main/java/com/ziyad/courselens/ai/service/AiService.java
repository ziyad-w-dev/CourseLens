package com.ziyad.courselens.ai.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.gemini.*;
import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
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

    public CourseAiResponse courseAiAnalyze(MultipartFile file, Program program) {
        String pdfText = extractCourseDetails(file);
        String promote = promptBuilder.build(pdfText, program);
        log.debug("promote sent to Gemini\n{}",promote);
        Part part = new Part(promote);
        Content content = new Content(List.of(part));
        ThinkingConfig thinkingConfig = new ThinkingConfig(0);
        GenerationConfig generationConfig = new GenerationConfig("application/json"
                , 0.3,thinkingConfig,schemaBuilder.build());
        GeminiRequest geminiRequest = new GeminiRequest(List.of(content),generationConfig);

        GeminiResponse response = restClient.post()
                .body(geminiRequest)
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


        throw new UnsupportedOperationException("AiService not built yet");
    }

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
}