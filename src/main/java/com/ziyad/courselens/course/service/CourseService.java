package com.ziyad.courselens.course.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.service.AiService;
import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.common.exception.InvalidFileException;
import com.ziyad.courselens.common.exception.ResourceNotFoundException;
import com.ziyad.courselens.common.mapper.CourselensMapper;
import com.ziyad.courselens.course.dto.CourseResponse;
import com.ziyad.courselens.course.dto.TopicResponse;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.entity.TopicFocus;
import com.ziyad.courselens.course.repository.CourseFocusRepository;
import com.ziyad.courselens.course.repository.CourseRepository;
import com.ziyad.courselens.course.repository.TopicFocusRepository;
import com.ziyad.courselens.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final TopicFocusRepository topicFocusRepository;
    private final CourseFocusRepository courseFocusRepository;
    private final CourseRepository courseRepository;
    private final CourselensMapper mapper;
    private final AiService aiService;
    private final CoursePersistenceService coursePersistenceService;


    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public CourseResponse uploadCourse(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        User doctor = getCurrentUser();

        // The program tells Gemini who the students are ("why this matters for a SWE student")
        CourseAiResponse aiData = aiService.courseAiAnalyze(file, doctor.getProgram());

        Course savedCourse = coursePersistenceService.saveCourse(aiData, doctor);

        return mapper.toCourseResponse(savedCourse);
    }

    public CourseResponse getCourseById(Long id) {
        User user = getCurrentUser();

        Course course = courseRepository.findByIdAndInstitution(id, user.getInstitution())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // course.getTopics() is ordered by id, which is the order of the course specification
        List<Topic> topics = course.getTopics();
        List<TopicResponse> topicResponses = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            TopicFocus focus = topicFocusRepository
                    .findByTopicAndTargetTrack(topic, user.getTrack())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No focus found for topic " + topic.getId() + " and track " + user.getTrack()));
            TopicResponse topicResponse = mapper.toTopicResponse(topic, focus);
            topicResponse.setSyllabusNumber(i + 1);
            topicResponses.add(topicResponse);
        }

        // description already holds "why this course matters for your program" (mapped by MapStruct)
        CourseResponse response = mapper.toCourseResponse(course);
        response.setTopics(topicResponses);

        // Summary counts for the student's track (same numbers as the course card)
        response.setMasterCount(countLevel(topicResponses, FocusLevel.MASTER_IT));
        response.setApplyCount(countLevel(topicResponses, FocusLevel.APPLY_IT));
        response.setKnowCount(countLevel(topicResponses, FocusLevel.KNOW_IT));

        // Course-level "why it matters for your track" — optional: if it's missing,
        // the page still works; only this section stays empty
        courseFocusRepository.findByCourseAndTargetTrack(course, user.getTrack())
                .ifPresent(courseFocus -> {
                    response.setFocusReason(courseFocus.getFocusReason());
                    response.setRealWorldExample(courseFocus.getRealWorldExample());
                });

        return response;
    }

    public List<CourseResponse> getCoursesForStudent() {
        User student = getCurrentUser();
        Track track = student.getTrack();

        return courseRepository.findByProgramAndInstitution(student.getProgram(), student.getInstitution())
                .stream()
                .map(course -> {
                    CourseResponse response = mapper.toCourseResponse(course);
                    response.setTopics(null);

                    // Card counts for the student's track
                    response.setMasterCount((int) topicFocusRepository
                            .countByTopicCourseAndTargetTrackAndFocusLevel(course, track, FocusLevel.MASTER_IT));
                    response.setApplyCount((int) topicFocusRepository
                            .countByTopicCourseAndTargetTrackAndFocusLevel(course, track, FocusLevel.APPLY_IT));
                    response.setKnowCount((int) topicFocusRepository
                            .countByTopicCourseAndTargetTrackAndFocusLevel(course, track, FocusLevel.KNOW_IT));

                    // "Start with" box: the first two MASTER_IT topics in syllabus order
                    response.setStartWith(topicFocusRepository
                            .findTop2ByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(
                                    course, track, FocusLevel.MASTER_IT)
                            .stream()
                            .map(focus -> focus.getTopic().getTitle())
                            .toList());

                    return response;
                })
                .collect(Collectors.toList());
    }

    private int countLevel(List<TopicResponse> topics, FocusLevel level) {
        return (int) topics.stream().filter(t -> t.getFocusLevel() == level).count();
    }

    public List<CourseResponse> getMyCoursesAsDoctor() {
        User doctor = getCurrentUser();
        return courseRepository.findByDoctorAndInstitution(doctor, doctor.getInstitution())
                .stream()
                .map(course -> {
                    CourseResponse response = mapper.toCourseResponse(course);
                    response.setTopics(null);
                    return response;
                })
                .collect(Collectors.toList());
    }
}