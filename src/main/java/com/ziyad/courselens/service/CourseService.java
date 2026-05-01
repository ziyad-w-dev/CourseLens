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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

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

        // Step 2 - extract text from PDF using PDFBox
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String pdfText = stripper.getText(document);
        document.close();

        // Step 3 - send text to AI and get back structured topics
        List<TopicAiResponse> aiTopics = aiService.extractTopics(pdfText);

        // Step 4 - build and save the Course entity
        Course course = new Course();
        course.setDoctor(doctor);
        course.setTitle(aiTopics.get(0).getCourseTitle());
        course.setCode(aiTopics.get(0).getCourseCode());
        course.setProgram(doctor.getProgram());
        courseRepository.save(course);

        // Step 5 - build and save each Topic entity
        for (TopicAiResponse aiTopic : aiTopics) {
            Topic topic = new Topic();
            topic.setTitle(aiTopic.getTitle());
            topic.setLectureHours(aiTopic.getLectureHours());
            topic.setLabHours(aiTopic.getLabHours());
            topic.setFocusLevel(aiTopic.getFocusLevel());
            topic.setFocusReason(aiTopic.getFocusReason());
            topic.setRealWorldExample(aiTopic.getRealWorldExample());
            topic.setTargetTrack(aiTopic.getTargetTrack());
            topic.setCourse(course);
            topicRepository.save(topic);
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

        // filter topics by student's track
        List<Topic> filteredTopics = course.getTopics()
                .stream()
                .filter(t -> t.getTargetTrack() == student.getTrack())
                .collect(Collectors.toList());

        course.setTopics(filteredTopics);
        return mapper.toCourseResponse(course);
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
