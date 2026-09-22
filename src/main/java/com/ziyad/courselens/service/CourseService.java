package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.*;
import com.ziyad.courselens.exception.InvalidFileException;
import com.ziyad.courselens.exception.ResourceNotFoundException;
import com.ziyad.courselens.mapper.CourselensMapper;
import com.ziyad.courselens.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final TopicFocusRepository topicFocusRepository;
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

        CourseAiResponse aiData = aiService.courseAiAnalyze(file);

        Course savedCourse = coursePersistenceService.saveCourse(aiData, doctor);

        return mapper.toCourseResponse(savedCourse);
    }

    public CourseResponse getCourseById(Long id) {
        User user = getCurrentUser();

        Course course = courseRepository.findByIdAndInstitution(id,user.getInstitution())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        List<TopicResponse> topicResponses = course.getTopics().stream()
                .map(topic -> {
                    TopicFocus focus = topicFocusRepository
                            .findByTopicAndTargetTrack(topic, user.getTrack())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "No focus found for topic " + topic.getId() + " and track " + user.getTrack()));
                    return mapper.toTopicResponse(topic, focus);
                })
                .collect(Collectors.toList());

        CourseResponse response = mapper.toCourseResponse(course);
        response.setTopics(topicResponses);
        return response;
    }

    public List<CourseResponse> getCoursesForStudent() {
        User student = getCurrentUser();
        return courseRepository.findByProgramAndInstitution(student.getProgram(),student.getInstitution())
                .stream()
                .map(course -> {
                    CourseResponse response = mapper.toCourseResponse(course);
                    response.setTopics(null);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getMyCoursesAsDoctor() {
        User doctor = getCurrentUser();
        return courseRepository.findByDoctorAndInstitution(doctor,doctor.getInstitution())
                .stream()
                .map(course -> {
                    CourseResponse response = mapper.toCourseResponse(course);
                    response.setTopics(null);
                    return response;
                })
                .collect(Collectors.toList());
    }
}
