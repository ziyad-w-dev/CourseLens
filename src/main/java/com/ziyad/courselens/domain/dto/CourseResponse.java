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
    private String level;
    private String description;
    private List<TopicResponse> topics;
    private LocalDateTime createdAt;


}
