package com.ziyad.courselens.course.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.service.AiService;
import com.ziyad.courselens.common.exception.CourseAlreadyExistsException;
import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.common.mapper.CourselensMapper;
import com.ziyad.courselens.course.dto.CourseResponse;
import com.ziyad.courselens.course.dto.TopicResponse;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.entity.TopicFocus;
import com.ziyad.courselens.course.repository.CourseFocusRepository;
import com.ziyad.courselens.course.repository.CourseRepository;
import com.ziyad.courselens.course.repository.TopicFocusRepository;
import com.ziyad.courselens.institution.entity.Institution;
import com.ziyad.courselens.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private TopicFocusRepository topicFocusRepository;
    @Mock
    private CourseFocusRepository courseFocusRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourselensMapper mapper;
    @Mock
    private AiService aiService;
    @Mock
    private CoursePersistenceService coursePersistenceService;
    @InjectMocks
    private CourseService courseService;

    private Institution institution;
    private User student;

    @BeforeEach
    void logInStudent() {
        institution = new Institution(1L, "UOH", "uoh.edu.sa");
        student = new User();
        student.setId(10L);
        student.setTrack(Track.BACKEND_DEVELOPER);
        student.setProgram(Program.SOFTWARE_ENGINEERING);
        student.setInstitution(institution);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(student, null, student.getAuthorities()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Topic topic(long id, String title) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setTitle(title);
        return topic;
    }

    private TopicFocus focus(Topic topic, FocusLevel level) {
        TopicFocus focus = new TopicFocus();
        focus.setTopic(topic);
        focus.setTargetTrack(Track.BACKEND_DEVELOPER);
        focus.setFocusLevel(level);
        return focus;
    }

    @Test
    void uploadCourse_duplicateCodeForProgram_throwsConflictAndSavesNothing() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf", new byte[]{1});
        CourseAiResponse aiData = new CourseAiResponse();
        aiData.setCourseCode("SENG 351");

        when(aiService.courseAiAnalyze(file, Program.SOFTWARE_ENGINEERING)).thenReturn(aiData);
        when(courseRepository.existsCourseByCodeAndProgramAndInstitution(
                "SENG 351", Program.SOFTWARE_ENGINEERING, institution)).thenReturn(true);

        assertThrows(CourseAlreadyExistsException.class, () -> courseService.uploadCourse(file));
        verify(coursePersistenceService, never()).saveCourse(any(), any());
    }

    @Test
    void getCoursesForStudent_fillsStartWithWithFirstMasterTopicTitle() {
        Course course = new Course();
        course.setId(5L);
        Topic sql = topic(1L, "SQL");

        when(courseRepository.findByProgramAndInstitution(Program.SOFTWARE_ENGINEERING, institution))
                .thenReturn(List.of(course));
        when(mapper.toCourseResponse(course)).thenReturn(new CourseResponse());
        when(topicFocusRepository.countByTopicCourseAndTargetTrackAndFocusLevel(course, Track.BACKEND_DEVELOPER, FocusLevel.MASTER_IT)).thenReturn(4L);
        when(topicFocusRepository.countByTopicCourseAndTargetTrackAndFocusLevel(course, Track.BACKEND_DEVELOPER, FocusLevel.APPLY_IT)).thenReturn(3L);
        when(topicFocusRepository.countByTopicCourseAndTargetTrackAndFocusLevel(course, Track.BACKEND_DEVELOPER, FocusLevel.KNOW_IT)).thenReturn(2L);
        when(topicFocusRepository.findFirstByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(
                course, Track.BACKEND_DEVELOPER, FocusLevel.MASTER_IT))
                .thenReturn(Optional.of(focus(sql, FocusLevel.MASTER_IT)));

        List<CourseResponse> result = courseService.getCoursesForStudent();

        assertEquals(1, result.size());
        CourseResponse card = result.get(0);
        assertEquals("SQL", card.getStartWith());
        assertEquals(4, card.getMasterCount());
        assertEquals(3, card.getApplyCount());
        assertEquals(2, card.getKnowCount());
    }

    @Test
    void getCoursesForStudent_noMasterTopics_startWithIsNull() {
        Course course = new Course();
        when(courseRepository.findByProgramAndInstitution(Program.SOFTWARE_ENGINEERING, institution))
                .thenReturn(List.of(course));
        when(mapper.toCourseResponse(course)).thenReturn(new CourseResponse());
        when(topicFocusRepository.findFirstByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(
                course, Track.BACKEND_DEVELOPER, FocusLevel.MASTER_IT)).thenReturn(Optional.empty());

        CourseResponse card = courseService.getCoursesForStudent().get(0);

        assertNull(card.getStartWith());
        assertEquals(0, card.getMasterCount());
    }

    @Test
    void getCourseById_numbersTopicsInSyllabusOrderAndCountsLevels() {
        Course course = new Course();
        course.setId(5L);
        Topic first = topic(11L, "Concepts");
        Topic second = topic(12L, "SQL");
        Topic third = topic(13L, "Transactions");
        course.setTopics(List.of(first, second, third));

        TopicFocus firstFocus = focus(first, FocusLevel.KNOW_IT);
        TopicFocus secondFocus = focus(second, FocusLevel.MASTER_IT);
        TopicFocus thirdFocus = focus(third, FocusLevel.MASTER_IT);

        when(courseRepository.findByIdAndInstitution(5L, institution)).thenReturn(Optional.of(course));
        when(topicFocusRepository.findByTopicAndTargetTrack(first, Track.BACKEND_DEVELOPER)).thenReturn(Optional.of(firstFocus));
        when(topicFocusRepository.findByTopicAndTargetTrack(second, Track.BACKEND_DEVELOPER)).thenReturn(Optional.of(secondFocus));
        when(topicFocusRepository.findByTopicAndTargetTrack(third, Track.BACKEND_DEVELOPER)).thenReturn(Optional.of(thirdFocus));
        when(mapper.toTopicResponse(any(Topic.class), any(TopicFocus.class))).thenAnswer(inv -> {
            Topic t = inv.getArgument(0);
            TopicFocus f = inv.getArgument(1);
            TopicResponse r = new TopicResponse();
            r.setId(t.getId());
            r.setTitle(t.getTitle());
            r.setFocusLevel(f.getFocusLevel());
            return r;
        });
        when(mapper.toCourseResponse(course)).thenReturn(new CourseResponse());
        when(courseFocusRepository.findByCourseAndTargetTrack(eq(course), eq(Track.BACKEND_DEVELOPER)))
                .thenReturn(Optional.empty());

        CourseResponse response = courseService.getCourseById(5L);

        List<TopicResponse> topics = response.getTopics();
        assertEquals(List.of(11L, 12L, 13L), topics.stream().map(TopicResponse::getId).toList());
        assertEquals(List.of(1, 2, 3), topics.stream().map(TopicResponse::getSyllabusNumber).toList());
        assertEquals(2, response.getMasterCount());
        assertEquals(0, response.getApplyCount());
        assertEquals(1, response.getKnowCount());
    }
}
