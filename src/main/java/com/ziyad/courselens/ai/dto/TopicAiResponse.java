package com.ziyad.courselens.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class TopicAiResponse {

    private String title;
    private int lectureHours;
    private int labHours;
    private List<TopicFocusAiResponse> focuses;

}
