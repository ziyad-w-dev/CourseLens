package com.ziyad.courselens.controllers;

import com.ziyad.courselens.domain.dto.TopicAiResponse;
import com.ziyad.courselens.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final AiService aiService;

//    @GetMapping("/ai")
//    public List<TopicAiResponse> testAi() {
//        String sampleText = """
//            Course Content
//
//            1. Overview of The SQL Query Language
//            2. Introduction to Relational Databases
//            3. Joins and Subqueries
//            4. Database Normalization
//            """;
//
//        return aiService.extractTopics(sampleText);
//    }
}
