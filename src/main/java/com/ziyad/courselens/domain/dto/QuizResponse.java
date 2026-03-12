package com.ziyad.courselens.domain.dto;

import lombok.Data;

@Data
public class QuizResponse {

    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

}
