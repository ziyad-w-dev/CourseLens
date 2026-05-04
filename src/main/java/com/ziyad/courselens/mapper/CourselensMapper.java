package com.ziyad.courselens.mapper;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourselensMapper {

    UserProfileResponse toUserProfileResponse(User user);

    CourseResponse toCourseResponse(Course course);

    QuizResponse toQuizResponse(Quiz quiz);

    QuizResultResponse toQuizResultResponse(QuizResult quizResult);

    @Mapping(source = "topic.id", target = "id")
    @Mapping(source = "topic.title", target = "title")
    @Mapping(source = "topic.lectureHours", target = "lectureHours")
    @Mapping(source = "topic.labHours", target = "labHours")
    @Mapping(source = "focus.focusLevel", target = "focusLevel")
    @Mapping(source = "focus.focusReason", target = "focusReason")
    @Mapping(source = "focus.realWorldExample", target = "realWorldExample")
    TopicResponse toTopicResponse(Topic topic, TopicFocus focus);
}
