package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.TopicAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    public List<TopicAiResponse> extractTopics(String pdfText) {
        // we will implement this soon
        return List.of();
    }
}
