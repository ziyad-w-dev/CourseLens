package com.ziyad.courselens.ai.service;

import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Track;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class CourseSchemaBuilder {

    private final List<String> tracksName = Arrays.stream(Track.values())
            .map(Track::name)
            .toList();

    private final List<String> focusLevelNames = Arrays.stream(FocusLevel.values())
            .map(FocusLevel::name)
            .toList();

    public Map<String, Object> build() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "courseTitle", Map.of("type", "STRING"),
                        "courseCode", Map.of("type", "STRING"),
                        "courseLevel", Map.of("type", "INTEGER"),
                        "programRelevance", Map.of("type", "STRING"),
                        "courseFocus", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "targetTrack", Map.of("type", "STRING", "enum", tracksName),
                                                "focusReason", Map.of("type", "STRING"),
                                                "realWorldExample", Map.of("type", "STRING")
                                        ),
                                        "required", List.of("targetTrack", "focusReason", "realWorldExample")
                                )
                        ), // closes courseFocus
                        "topics", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "title", Map.of("type", "STRING"),
                                                "lectureHours", Map.of("type", "INTEGER"),
                                                "labHours", Map.of("type", "INTEGER"),
                                                "focuses", Map.of(
                                                        "type", "ARRAY",
                                                        "items", Map.of(
                                                                "type", "OBJECT",
                                                                "properties", Map.of(
                                                                        "targetTrack", Map.of("type", "STRING", "enum", tracksName),
                                                                        "focusLevel", Map.of("type", "STRING", "enum", focusLevelNames),
                                                                        "focusReason", Map.of("type", "STRING"),
                                                                        "realWorldExample", Map.of("type", "STRING")
                                                                ),
                                                                "required", List.of("targetTrack", "focusLevel",
                                                                        "focusReason", "realWorldExample")
                                                        )
                                                ) // closes focuses
                                        ),
                                        "required", List.of("title", "lectureHours", "labHours", "focuses")
                                )
                        ) // closes topics
                ), // closes top properties
                "required", List.of("courseTitle", "courseCode", "courseLevel",
                        "programRelevance", "courseFocus", "topics")
        );
    }
}
