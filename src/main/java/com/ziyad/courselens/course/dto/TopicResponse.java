package com.ziyad.courselens.course.dto;


import com.ziyad.courselens.common.domain.FocusLevel;

import lombok.Data;

@Data
public class TopicResponse {

    private Long id;
    // 1-based position of the topic in the course specification ("#05" in the UI)
    private int syllabusNumber;
    private String title;
    private int lectureHours;
    private int labHours;
    private FocusLevel focusLevel;
    private String focusReason;
    private String realWorldExample;

}