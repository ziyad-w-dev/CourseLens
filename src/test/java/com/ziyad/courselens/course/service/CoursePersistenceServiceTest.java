package com.ziyad.courselens.course.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.dto.TopicAiResponse;
import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.repository.CourseRepository;
import com.ziyad.courselens.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CoursePersistenceServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private CoursePersistenceService coursePersistenceService;

    private List<String> savedTopicTitles(String... aiTitles) {
        CourseAiResponse aiData = new CourseAiResponse();
        aiData.setCourseTitle("Database Management Systems");
        aiData.setCourseFocus(List.of());
        aiData.setTopics(Arrays.stream(aiTitles).map(title -> {
            TopicAiResponse topic = new TopicAiResponse();
            topic.setTitle(title);
            topic.setFocuses(List.of());
            return topic;
        }).toList());

        User doctor = new User();
        doctor.setProgram(Program.SOFTWARE_ENGINEERING);

        Course course = coursePersistenceService.saveCourse(aiData, doctor);
        return course.getTopics().stream().map(Topic::getTitle).toList();
    }

    @Test
    void saveCourse_keepsOnlyTheTopicHeading() {
        List<String> titles = savedTopicTitles(
                "Introduction to Databases: • Types of Databases • Basic Definitions",
                "Overview of The SQL Query Language  SQL Data Definition  Basic Query",
                "Normalization • 1NF • 2NF",
                "Transaction Management");

        assertEquals(List.of(
                "Introduction to Databases",
                "Overview of The SQL Query Language",
                "Normalization",
                "Transaction Management"), titles);
    }
}
