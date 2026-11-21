package com.ziyad.courselens.ai.dto;

import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Track;
import lombok.Data;

@Data
public class TopicFocusAiResponse {

    private FocusLevel focusLevel;
    private String focusReason;
    private String realWorldExample;
    private Track targetTrack;
}
