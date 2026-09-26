package com.ziyad.courselens.domain.dto;


import com.ziyad.courselens.domain.entity.Program;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseResponse {

    private Long id;
    private String title;
    private String code;
    private Program program;
    private int level;
    private String description;
    private String focusReason;
    private String realWorldExample;
    // Student list only: how many topics fall in each focus level for the student's track
    private Integer masterCount;
    private Integer applyCount;
    private Integer knowCount;
    private List<TopicResponse> topics;
    private LocalDateTime createdAt;

}
