package com.ziyad.courselens.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseAiResponse {

    private String courseTitle;
    private String courseCode;
    private List<CourseFocusAiResponse> courseFocus;
    private List<TopicAiResponse> topics;

}