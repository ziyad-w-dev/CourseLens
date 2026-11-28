package com.ziyad.courselens.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.exception.InvalidFileException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private RestClient restClient;
    @Mock
    private CoursePromptBuilder promptBuilder;
    @Mock
    private CourseSchemaBuilder schemaBuilder;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private AiService aiService;

    @Test
    void courseAiAnalyze_notAPdf_throwsInvalidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf",
                "this is not a pdf".getBytes());

        assertThrows(InvalidFileException.class,
                () -> aiService.courseAiAnalyze(file, Program.SOFTWARE_ENGINEERING));
        verifyNoInteractions(restClient);
    }

    @Test
    void courseAiAnalyze_pdfWithNoText_throwsInvalidFileWithoutCallingGemini() throws IOException {
        // A valid PDF with one empty page: what a scanned / image-only spec looks like to the text stripper
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(out);
        }
        MockMultipartFile file = new MockMultipartFile("file", "scan.pdf", "application/pdf", out.toByteArray());

        InvalidFileException ex = assertThrows(InvalidFileException.class,
                () -> aiService.courseAiAnalyze(file, Program.SOFTWARE_ENGINEERING));
        assertEquals("File is not a readable PDF", ex.getMessage());
        verifyNoInteractions(restClient);
    }
}
