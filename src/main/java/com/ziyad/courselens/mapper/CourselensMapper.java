package com.ziyad.courselens.mapper;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.*;

public interface CourselensMapper {

    UserProfileResponse toUserProfileResponse(User user);

    CourseResponse toCourseResponse(Course course);

    TopicResponse toTopicResponse(Topic topic);

    QuizResponse toQuizResponse(Quiz quiz);

    QuizResultResponse toQuizResultResponse(QuizResult quizResult);
}
