package com.ziyad.courselens.course.repository;

import com.ziyad.courselens.common.domain.FocusLevel;
import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.entity.TopicFocus;
import com.ziyad.courselens.institution.entity.Institution;
import com.ziyad.courselens.institution.repository.InstitutionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TopicFocusRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private InstitutionRepository institutionRepository;
    @Autowired
    private TopicFocusRepository topicFocusRepository;

    private void addTopic(Course course, String title, FocusLevel backendLevel) {
        Topic topic = new Topic();
        topic.setTitle(title);
        topic.setCourse(course);
        for (Track track : Track.values()) {
            TopicFocus focus = new TopicFocus();
            focus.setTopic(topic);
            focus.setTargetTrack(track);
            // only the backend track gets the given level; the others are KNOW_IT
            focus.setFocusLevel(track == Track.BACKEND_DEVELOPER ? backendLevel : FocusLevel.KNOW_IT);
            focus.setFocusReason("reason");
            focus.setRealWorldExample("example");
            topic.getFocuses().add(focus);
        }
        course.getTopics().add(topic);
    }

    @Test
    void findFirstMasterTopic_returnsFirstInSyllabusOrderForThatTrackOnly() {
        Institution institution = institutionRepository.save(new Institution(null, "UOH", "uoh.edu.sa"));
        Course course = new Course();
        course.setTitle("Database Management Systems");
        course.setCode("SENG 351");
        course.setProgram(Program.SOFTWARE_ENGINEERING);
        course.setInstitution(institution);
        addTopic(course, "Concepts", FocusLevel.KNOW_IT);
        addTopic(course, "SQL", FocusLevel.MASTER_IT);
        addTopic(course, "ER Modeling", FocusLevel.APPLY_IT);
        addTopic(course, "Transactions", FocusLevel.MASTER_IT);
        addTopic(course, "Indexing", FocusLevel.MASTER_IT);
        courseRepository.save(course);

        Optional<String> backend = topicFocusRepository
                .findFirstByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(
                        course, Track.BACKEND_DEVELOPER, FocusLevel.MASTER_IT)
                .map(f -> f.getTopic().getTitle());
        Optional<TopicFocus> frontend = topicFocusRepository
                .findFirstByTopicCourseAndTargetTrackAndFocusLevelOrderByTopicIdAsc(
                        course, Track.FRONTEND_DEVELOPER, FocusLevel.MASTER_IT);

        assertEquals(Optional.of("SQL"), backend);
        assertTrue(frontend.isEmpty());
    }
}
