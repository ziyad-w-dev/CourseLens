package com.ziyad.courselens.repository;

import com.ziyad.courselens.domain.entity.Course;
import com.ziyad.courselens.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository{

    List<Course> findByDoctor(User doctor);
    Optional<Course> findByCode(String code);
}
