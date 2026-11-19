package com.ziyad.courselens.ai.dto;

import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Track;
import lombok.Data;

@Data
public class TopicAiResponse {

    private String title;
    private int lectureHours;
    private int labHours;
    private FocusLevel focusLevel;
    private String focusReason;
    private String realWorldExample;
    private Track targetTrack;

}
