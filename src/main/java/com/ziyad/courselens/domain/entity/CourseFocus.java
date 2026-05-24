package com.ziyad.courselens.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_focus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseFocus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Track targetTrack;

    @Column(nullable = false, length = 1000)
    private String focusReason;

    @Column(nullable = false, length = 1000)
    private String realWorldExample;
}
