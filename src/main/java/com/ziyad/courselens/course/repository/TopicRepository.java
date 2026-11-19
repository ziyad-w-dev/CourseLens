package com.ziyad.courselens.course.repository;


import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByCourse(Course course);
}
