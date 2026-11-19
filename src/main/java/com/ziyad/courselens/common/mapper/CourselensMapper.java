package com.ziyad.courselens.common.mapper;

import com.ziyad.courselens.course.dto.CourseResponse;
import com.ziyad.courselens.course.dto.TopicResponse;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.entity.TopicFocus;
import com.ziyad.courselens.user.dto.UserProfileResponse;
import com.ziyad.courselens.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourselensMapper {

    UserProfileResponse toUserProfileResponse(User user);

    CourseResponse toCourseResponse(Course course);


    @Mapping(source = "topic.id", target = "id")
    @Mapping(source = "topic.title", target = "title")
    @Mapping(source = "topic.lectureHours", target = "lectureHours")
    @Mapping(source = "topic.labHours", target = "labHours")
    @Mapping(source = "focus.focusLevel", target = "focusLevel")
    @Mapping(source = "focus.focusReason", target = "focusReason")
    @Mapping(source = "focus.realWorldExample", target = "realWorldExample")
    TopicResponse toTopicResponse(Topic topic, TopicFocus focus);
}
