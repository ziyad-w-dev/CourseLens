package com.ziyad.courselens.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "topic_focus")
@Data
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

    private String focusReason;

    private String realWorldExample;

}
