package com.ziyad.courselens.repository;

import com.ziyad.courselens.domain.entity.Quiz;
import com.ziyad.courselens.domain.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository {

    List<Quiz> findByTopic(Topic topic);
}
