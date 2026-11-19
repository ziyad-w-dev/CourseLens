package com.ziyad.courselens.course.repository;

import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.CourseFocus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseFocusRepository extends JpaRepository<CourseFocus, Long> {

    Optional<CourseFocus> findByCourseAndTargetTrack(Course course, Track targetTrack);
}