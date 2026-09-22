package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.CourseAiResponse;
import com.ziyad.courselens.domain.dto.CourseFocusAiResponse;
import com.ziyad.courselens.domain.dto.TopicAiResponse;
import com.ziyad.courselens.domain.entity.*;
import com.ziyad.courselens.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursePersistenceService {

    private final CourseRepository courseRepository;

    @Transactional
    public Course saveCourse(CourseAiResponse aiData, User doctor){

        // Step 3 - save the course itself
        Course course = new Course();
        course.setDoctor(doctor);
        course.setTitle(aiData.getCourseTitle()); // we will take it from Ai response
        course.setCode(aiData.getCourseCode()); // we will take it from Ai response
        course.setProgram(doctor.getProgram());
        course.setInstitution(doctor.getInstitution());

        // Step 4 - save the 5 course-level focus rows (one per track)
        for (CourseFocusAiResponse aiFocus : aiData.getCourseFocus()) { // to loop through the Ai response and store the result in the Ai response DTO
            CourseFocus courseFocus = new CourseFocus(); // create the course focus object
            courseFocus.setCourse(course); // set the course that we are dealing with
            courseFocus.setTargetTrack(aiFocus.getTargetTrack());// get the info from the Ai response
            courseFocus.setFocusReason(aiFocus.getFocusReason());
            courseFocus.setRealWorldExample(aiFocus.getRealWorldExample());
            course.getCourseFocuses().add(courseFocus);
        }


        // Step 5 - group topic entries by title (each title has 5 entries, one per track)
        // key and value pair were the key will be the topic name and the value will be the 5 entries for the same topic and just differ in the track
        Map<String, List<TopicAiResponse>> grouped = aiData.getTopics().stream()
                .collect(Collectors.groupingBy(TopicAiResponse::getTitle));

        // Step 6 - for each topic group: save the Topic, then save its 5 TopicFocus rows
        for (Map.Entry<String, List<TopicAiResponse>> entry : grouped.entrySet()) {
            List<TopicAiResponse> group = entry.getValue();
            TopicAiResponse first = group.get(0);

            Topic topic = new Topic();
            topic.setTitle(first.getTitle());
            topic.setLectureHours(first.getLectureHours());
            topic.setLabHours(first.getLabHours());
            topic.setCourse(course);


            for (TopicAiResponse ai : group) {
                TopicFocus focus = new TopicFocus();
                focus.setTopic(topic);
                focus.setTargetTrack(ai.getTargetTrack());
                focus.setFocusLevel(ai.getFocusLevel());
                focus.setFocusReason(ai.getFocusReason());
                focus.setRealWorldExample(ai.getRealWorldExample());
                topic.getFocuses().add(focus);
            }
            course.getTopics().add(topic);
        }

        courseRepository.save(course);
        return course;
    }
}
