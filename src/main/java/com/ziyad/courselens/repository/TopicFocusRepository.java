package com.ziyad.courselens.repository;


import com.ziyad.courselens.domain.entity.FocusLevel;
import com.ziyad.courselens.domain.entity.Topic;
import com.ziyad.courselens.domain.entity.TopicFocus;
import com.ziyad.courselens.domain.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicFocusRepository extends JpaRepository<TopicFocus, Long> {

    //List<FocusLevel> findFocusAndTrack(Topic topic, Track track);

    Optional<TopicFocus> findByTopicAndTargetTrack(Topic topic, Track targetTrack);

    List<TopicFocus> findByTopic(Topic topic);


}
