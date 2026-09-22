package com.ziyad.courselens.repository;

import com.ziyad.courselens.domain.entity.Course;
import com.ziyad.courselens.domain.entity.Institution;
import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByDoctorAndInstitution(User doctor,Institution institution) ;
    List<Course> findByProgramAndInstitution(Program program,Institution institution);
    Optional<Course> findByIdAndInstitution(Long id, Institution institution);
}
