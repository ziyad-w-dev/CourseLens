package com.ziyad.courselens.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topic_focus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicFocus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Topic topic;

    @Enumerated(EnumType.STRING)
    private Track targetTrack;

    @Enumerated(EnumType.STRING)
    private FocusLevel focusLevel;

    @Column(nullable = false, length = 1000)
    private String focusReason;

    @Column(nullable = false, length = 1000)
    private String realWorldExample;

}
