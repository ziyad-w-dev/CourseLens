package com.ziyad.courselens.domain.dto;

import lombok.Data;

@Data
public class QuizResultResponse {

    private String question;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean correct;

}
