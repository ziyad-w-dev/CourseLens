package com.ziyad.courselens.controllers;

import com.ziyad.courselens.domain.dto.CourseResponse;
import com.ziyad.courselens.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('DOCTOR')")
    public CourseResponse uploadCourse(@RequestParam("file") MultipartFile file) {
        return courseService.uploadCourse(file);
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public List<CourseResponse> getCoursesForStudent() {
        return courseService.getCoursesForStudent();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public CourseResponse getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<CourseResponse> getMyCoursesAsDoctor() {
        return courseService.getMyCoursesAsDoctor();
    }
}