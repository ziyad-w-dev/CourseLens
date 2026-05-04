package com.ziyad.courselens.domain.dto;


import com.ziyad.courselens.domain.entity.FocusLevel;
import lombok.Data;

@Data
public class TopicResponse {

    private Long id;
    private String title;
    private int lectureHours;
    private int labHours;
    private FocusLevel focusLevel;
    private String focusReason;
    private String realWorldExample;

}