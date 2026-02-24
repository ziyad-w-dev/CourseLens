package com.ziyad.courselens.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Database Management Systems

    @Column(nullable = false, unique = true)
    private String code; // SENG351

    private String program; // Software Engineering

    private int level; // 5

    private String description;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor; // who uploaded this course

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Topic> topics = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}
