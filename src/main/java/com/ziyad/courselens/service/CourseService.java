package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.*;
import com.ziyad.courselens.mapper.CourselensMapper;
import com.ziyad.courselens.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import com.ziyad.courselens.domain.entity.TopicFocus;
import com.ziyad.courselens.repository.TopicFocusRepository;
import java.util.Map;
import java.util.stream.Collectors;
import com.ziyad.courselens.domain.dto.TopicResponse;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CourseService {

    private final TopicFocusRepository topicFocusRepository;
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CourselensMapper mapper;
    private final AiService aiService;

    // ─── Helper: get current logged in user ───────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ─── Doctor: upload PDF and create course ─────────────────────────
    public CourseResponse uploadCourse(MultipartFile file) throws IOException {

        // Step 1 - get the doctor
        User doctor = getCurrentUser();

        // Step 2 - extract text from PDF
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String pdfText = stripper.getText(document);
        document.close();

        // Step 3 - AI returns one entry per (topic, track) combination
        List<TopicAiResponse> aiTopics = aiService.extractTopics(pdfText);

        // Step 4 - build and save the Course
        Course course = new Course();
        course.setDoctor(doctor);
        course.setTitle(aiTopics.get(0).getCourseTitle());
        course.setCode(aiTopics.get(0).getCourseCode());
        course.setProgram(doctor.getProgram());
        courseRepository.save(course);

        // Step 5 - group AI responses by topic title
        // (e.g. all 5 "SQL Joins" entries grouped together)
        Map<String, List<TopicAiResponse>> grouped = aiTopics.stream()
                .collect(Collectors.groupingBy(TopicAiResponse::getTitle));

        // Step 6 - for each group, save ONE Topic + multiple TopicFocus rows
        for (Map.Entry<String, List<TopicAiResponse>> entry : grouped.entrySet()) {
            List<TopicAiResponse> group = entry.getValue();
            TopicAiResponse first = group.get(0);

            // Save the Topic (shared info)
            Topic topic = new Topic();
            topic.setTitle(first.getTitle());
            topic.setLectureHours(first.getLectureHours());
            topic.setLabHours(first.getLabHours());
            topic.setCourse(course);
            topicRepository.save(topic);

            // Save one TopicFocus per track
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

    // ─── Student: get all courses matching their program ──────────────
    public List<CourseResponse> getCoursesForStudent() {
        User student = getCurrentUser();
        return courseRepository.findByProgram(student.getProgram())
                .stream()
                .map(mapper::toCourseResponse)
                .collect(Collectors.toList());
    }

    // ─── Student: get one course (topics filtered by their track) ─────
    public CourseResponse getCourseById(Long id) {
        User student = getCurrentUser();

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // For each topic, find the focus matching the student's track,
        // then build a TopicResponse merging both
        List<TopicResponse> topicResponses = course.getTopics().stream()
                .map(topic -> {
                    TopicFocus focus = topicFocusRepository
                            .findByTopicAndTargetTrack(topic, student.getTrack())
                            .orElseThrow(() -> new RuntimeException(
                                    "No focus found for topic " + topic.getId()));
                    return mapper.toTopicResponse(topic, focus);
                })
                .collect(Collectors.toList());

        CourseResponse response = mapper.toCourseResponse(course);
        response.setTopics(topicResponses);
        return response;
    }

    // ─── Doctor: get their own courses ────────────────────────────────
    public List<CourseResponse> getMyCoursesAsDoctor() {
        User doctor = getCurrentUser();
        return courseRepository.findByDoctor(doctor)
                .stream()
                .map(mapper::toCourseResponse)
                .collect(Collectors.toList());
    }
}
