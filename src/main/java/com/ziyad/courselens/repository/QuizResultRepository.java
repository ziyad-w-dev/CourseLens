package com.ziyad.courselens.repository;

import com.ziyad.courselens.domain.entity.Quiz;
import com.ziyad.courselens.domain.entity.QuizResult;
import com.ziyad.courselens.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByStudent(User student);
    List<QuizResult> findByQuiz(Quiz quiz);
}
