package com.ziyad.courselens.domain.dto;

import lombok.Data;

@Data
public class QuizSubmitRequest {

    private Long quizId;
    private String selectedAnswer;
    
}