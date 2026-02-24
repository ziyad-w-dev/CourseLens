package com.ziyad.courselens.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Program program; // null for doctors

    @Enumerated(EnumType.STRING)
    private Role role; // STUDENT or DOCTOR

    @Enumerated(EnumType.STRING)
    private Track track; // NULL for Doctors

    @CreationTimestamp
    private LocalDateTime createdAt;

}
