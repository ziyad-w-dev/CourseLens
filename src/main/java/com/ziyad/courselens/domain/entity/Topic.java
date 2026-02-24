package com.ziyad.courselens.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // SQL Query Language

    private int lectureHours;

    private int labHours;

    // AI generated fields
    private String focusLevel; // MASTER_IT, APPLY_IT, KNOW_IT

    private String focusReason; // why it matters for this track

    private String realWorldExample;

    @Enumerated(EnumType.STRING)
    private Track targetTrack; // which track this analysis is for

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Quiz> quizzes = new ArrayList<>();
}
