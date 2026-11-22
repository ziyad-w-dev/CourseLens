package com.ziyad.courselens.ai.service;

import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class CoursePromptBuilder {

    //                            to go through this array element by element
    private final String tracks = Arrays.stream(Track.values())
            .map(Track::name)// to take each element by itself
            .collect(Collectors.joining(", "));// collect all the element on one String and separate them by ", "

    public String build(String pdfText, Program program){
        return """
        You are an expert curriculum designer for a personalized learning platform.
        The students are enrolled in the {PROGRAM} program.
        Each student follows ONE career track. The career tracks are: {TRACKS}.

        Read the course specification below and analyze it.

        FIELD INSTRUCTIONS:

        courseTitle: the course name exactly as written in the document.
        courseCode: the course code exactly as written (example: SENG 351).
        courseLevel: the course level number (not the year). If not found, use 0.

        programRelevance: 2-3 sentences explaining why this course matters
        for a {PROGRAM} student. Do NOT mention any career track here.

        courseFocus: exactly ONE entry for EACH track listed above.
          - focusReason: why this course is useful for that track.
          - realWorldExample: one real job situation where that track uses this course.
          Always be positive. Never suggest skipping the course.

        topics: one entry for EACH topic in the course.
          - title: the topic heading only, as written in the document.
                Do NOT include the subtopics listed after the heading.
                Do NOT include the trailing colon.
                Example: "Introduction: Importance of RE, Misconceptions, ..."
                → title: "Introduction"
          - lectureHours / labHours: from the document. If not found, use 0.
          - focuses: exactly ONE entry for EACH track listed above.
              - focusLevel:
                  MASTER_IT = core to this track, must know deeply
                  APPLY_IT  = will use it in practice, needs working knowledge
                  KNOW_IT   = good to understand, rarely used directly
              - focusReason: why this level fits this track for this topic.
              - realWorldExample: one real situation where this track uses this topic.
              The reasons must be different for each track.

        RULES:
        - Take topics ONLY from the "Course Content" section.
        - Ignore headers, footers, Arabic text, learning outcomes,
          assessment, references, and approval sections.
        - Use the exact track and focus level names given above.

        COURSE SPECIFICATION:
        {PDF_TEXT}
        """
                .replace("{PROGRAM}", program.name())
                .replace("{TRACKS}", tracks)
                .replace("{PDF_TEXT}", pdfText);
    }
}