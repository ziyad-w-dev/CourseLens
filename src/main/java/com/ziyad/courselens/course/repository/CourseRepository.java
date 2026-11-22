package com.ziyad.courselens.course.repository;

import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.institution.entity.Institution;
import com.ziyad.courselens.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByDoctorAndInstitution(User doctor,Institution institution) ;
    List<Course> findByProgramAndInstitution(Program program,Institution institution);
    Optional<Course> findByIdAndInstitution(Long id, Institution institution);
    boolean existsCourseByCodeAndProgramAndInstitution(String code, Program program, Institution institution);
}
