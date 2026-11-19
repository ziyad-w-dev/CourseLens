package com.ziyad.courselens.ai.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.gemini.Candidate;
import com.ziyad.courselens.ai.gemini.Content;
import com.ziyad.courselens.ai.gemini.GeminiRequest;
import com.ziyad.courselens.ai.gemini.GeminiResponse;
import com.ziyad.courselens.ai.gemini.Part;
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

    public CourseAiResponse courseAiAnalyze(MultipartFile file, Program program) {
        extractCourseDetails(file);
        Part part = new Part("Hello Gemini");
        Content content = new Content(List.of(part));
        GeminiRequest geminiRequest = new GeminiRequest(List.of(content));

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