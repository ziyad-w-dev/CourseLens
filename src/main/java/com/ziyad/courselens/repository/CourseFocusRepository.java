package com.ziyad.courselens.repository;

import com.ziyad.courselens.domain.entity.Course;
import com.ziyad.courselens.domain.entity.CourseFocus;
import com.ziyad.courselens.domain.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseFocusRepository extends JpaRepository<CourseFocus, Long> {

    Optional<CourseFocus> findByCourseAndTargetTrack(Course course, Track targetTrack);
}