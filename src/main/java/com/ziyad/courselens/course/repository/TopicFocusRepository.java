package com.ziyad.courselens.course.repository;


import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.entity.TopicFocus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicFocusRepository extends JpaRepository<TopicFocus, Long> {

    //List<FocusLevel> findFocusAndTrack(Topic topic, Track track);

    Optional<TopicFocus> findByTopicAndTargetTrack(Topic topic, Track targetTrack);

    List<TopicFocus> findByTopic(Topic topic);

    long countByTopicCourseAndTargetTrackAndFocusLevel(Course course, Track targetTrack, FocusLevel focusLevel);

    // First two topics of a focus level in syllabus order ("Start with" box on the course card)
    List<TopicFocus> findTop2ByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(
            Course course, Track targetTrack, FocusLevel focusLevel);


}
