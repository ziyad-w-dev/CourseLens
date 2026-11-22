package com.ziyad.courselens.course.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.service.AiService;
import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.common.exception.CourseAlreadyExistsException;
import com.ziyad.courselens.common.exception.InvalidFileException;
import com.ziyad.courselens.common.exception.ResourceNotFoundException;
import com.ziyad.courselens.common.mapper.CourselensMapper;
import com.ziyad.courselens.course.dto.CourseResponse;
import com.ziyad.courselens.course.dto.TopicResponse;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.TopicFocus;
import com.ziyad.courselens.course.repository.CourseFocusRepository;
import com.ziyad.courselens.course.repository.CourseRepository;
import com.ziyad.courselens.course.repository.TopicFocusRepository;
import com.ziyad.courselens.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

        if(courseRepository.existsCourseByCodeAndProgramAndInstitution(
                aiData.getCourseCode(),doctor.getProgram(),doctor.getInstitution())) {
            log.info("Duplicate upload rejected: {} for program {} by doctor {}",
                    aiData.getCourseCode(), doctor.getProgram(), doctor.getId());
            throw new CourseAlreadyExistsException("Course " + aiData.getCourseCode() + " already exists for this program");
        }

        Course savedCourse = coursePersistenceService.saveCourse(aiData, doctor);

        return mapper.toCourseResponse(savedCourse);
    }

    public CourseResponse getCourseById(Long id) {
        User user = getCurrentUser();

        Course course = courseRepository.findByIdAndInstitution(id, user.getInstitution())
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

        // description already holds "why this course matters for your program" (mapped by MapStruct)
        CourseResponse response = mapper.toCourseResponse(course);
        response.setTopics(topicResponses);

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

                    return response;
                })
                .collect(Collectors.toList());
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