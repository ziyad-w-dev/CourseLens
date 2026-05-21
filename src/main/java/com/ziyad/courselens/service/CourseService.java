package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.*;
import com.ziyad.courselens.exception.ResourceNotFoundException;
import com.ziyad.courselens.mapper.CourselensMapper;
import com.ziyad.courselens.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final TopicFocusRepository topicFocusRepository;
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CourselensMapper mapper;
    private final AiService aiService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public CourseResponse uploadCourse(MultipartFile file) throws IOException {
        User doctor = getCurrentUser();

        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String pdfText = stripper.getText(document);
        document.close();

        List<TopicAiResponse> aiTopics = aiService.extractTopics(pdfText);

        Course course = new Course();
        course.setDoctor(doctor);
        course.setTitle(aiTopics.get(0).getCourseTitle());
        course.setCode(aiTopics.get(0).getCourseCode());
        course.setProgram(doctor.getProgram());
        courseRepository.save(course);

        Map<String, List<TopicAiResponse>> grouped = aiTopics.stream()
                .collect(Collectors.groupingBy(TopicAiResponse::getTitle));

        for (Map.Entry<String, List<TopicAiResponse>> entry : grouped.entrySet()) {
            List<TopicAiResponse> group = entry.getValue();
            TopicAiResponse first = group.get(0);

            Topic topic = new Topic();
            topic.setTitle(first.getTitle());
            topic.setLectureHours(first.getLectureHours());
            topic.setLabHours(first.getLabHours());
            topic.setCourse(course);
            topicRepository.save(topic);

            for (TopicAiResponse ai : group) {
                TopicFocus focus = new TopicFocus();
                focus.setTopic(topic);
                focus.setTargetTrack(ai.getTargetTrack());
                focus.setFocusLevel(ai.getFocusLevel());
                focus.setFocusReason(ai.getFocusReason());
                focus.setRealWorldExample(ai.getRealWorldExample());
                topicFocusRepository.save(focus);
            }
        }

        return mapper.toCourseResponse(course);
    }

    public List<CourseResponse> getCoursesForStudent() {
        User student = getCurrentUser();
        return courseRepository.findByProgram(student.getProgram())
                .stream()
                .map(course -> {
                    CourseResponse response = mapper.toCourseResponse(course);
                    response.setTopics(null);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(Long id) {
        User student = getCurrentUser();

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        List<TopicResponse> topicResponses = course.getTopics().stream()
                .map(topic -> {
                    TopicFocus focus = topicFocusRepository
                            .findByTopicAndTargetTrack(topic, student.getTrack())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "No focus found for topic " + topic.getId() + " and track " + student.getTrack()));
                    return mapper.toTopicResponse(topic, focus);
                })
                .collect(Collectors.toList());

        CourseResponse response = mapper.toCourseResponse(course);
        response.setTopics(topicResponses);
        return response;
    }

    public List<CourseResponse> getMyCoursesAsDoctor() {
        User doctor = getCurrentUser();
        return courseRepository.findByDoctor(doctor)
                .stream()
                .map(course -> {
                    CourseResponse response = mapper.toCourseResponse(course);
                    response.setTopics(null);
                    return response;
                })
                .collect(Collectors.toList());
    }
}
