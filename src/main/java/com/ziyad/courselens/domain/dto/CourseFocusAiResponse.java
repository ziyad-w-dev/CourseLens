package com.ziyad.courselens.domain.dto;

import com.ziyad.courselens.domain.entity.Track;
import lombok.Data;

@Data
public class CourseFocusAiResponse {

    private Track targetTrack;
    private String focusReason;
    private String realWorldExample;
    
}