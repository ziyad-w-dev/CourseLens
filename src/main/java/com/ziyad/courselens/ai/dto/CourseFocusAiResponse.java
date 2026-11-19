package com.ziyad.courselens.ai.dto;

import com.ziyad.courselens.common.domain.Track;
import lombok.Data;

@Data
public class CourseFocusAiResponse {

    private Track targetTrack;
    private String focusReason;
    private String realWorldExample;
    
}