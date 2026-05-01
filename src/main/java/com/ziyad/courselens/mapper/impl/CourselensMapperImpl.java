package com.ziyad.courselens.mapper.impl;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.*;
import com.ziyad.courselens.mapper.CourselensMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourselensMapperImpl implements CourselensMapper {

    private final ModelMapper modelMapper;

    @Override
    public UserProfileResponse toUserProfileResponse(User user) {
        return modelMapper.map(user, UserProfileResponse.class);
    }

    @Override
    public CourseResponse toCourseResponse(Course course) {
        return modelMapper.map(course, CourseResponse.class);
    }

    @Override
    public TopicResponse toTopicResponse(Topic topic) {
        return modelMapper.map(topic, TopicResponse.class);
    }

    @Override
    public QuizResponse toQuizResponse(Quiz quiz) {
        return modelMapper.map(quiz, QuizResponse.class);
    }

    @Override
    public QuizResultResponse toQuizResultResponse(QuizResult quizResult) {
        return modelMapper.map(quizResult, QuizResultResponse.class);
    }
}
