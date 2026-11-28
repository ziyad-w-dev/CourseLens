package com.ziyad.courselens.course.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.dto.CourseFocusAiResponse;
import com.ziyad.courselens.ai.dto.TopicAiResponse;
import com.ziyad.courselens.ai.dto.TopicFocusAiResponse;
import com.ziyad.courselens.course.entity.Course;
import com.ziyad.courselens.course.entity.CourseFocus;
import com.ziyad.courselens.course.entity.Topic;
import com.ziyad.courselens.course.entity.TopicFocus;
import com.ziyad.courselens.course.repository.CourseRepository;
import com.ziyad.courselens.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoursePersistenceService {

    private final CourseRepository courseRepository;

    @Transactional
    public Course saveCourse(CourseAiResponse aiData, User doctor){

        // Step 1 - build the course (program & institution come from the doctor, not the AI)
        Course course = new Course();
        course.setDoctor(doctor);
        course.setTitle(aiData.getCourseTitle()); // we will take it from Ai response
        course.setCode(aiData.getCourseCode());// we will take it from Ai response
        course.setLevel(aiData.getCourseLevel() != null ? aiData.getCourseLevel() : 0);// short if-else
        course.setDescription(aiData.getProgramRelevance());
        course.setProgram(doctor.getProgram());
        course.setInstitution(doctor.getInstitution());

        // Step 2 - build course-level focuses (one per track)
        for (CourseFocusAiResponse aiFocus : aiData.getCourseFocus()) { // to loop through the Ai response and store the result in the Ai response DTO
            CourseFocus courseFocus = new CourseFocus(); // create the course focus object
            courseFocus.setCourse(course); // set the course that we are dealing with
            courseFocus.setTargetTrack(aiFocus.getTargetTrack());// get the info from the Ai response
            courseFocus.setFocusReason(aiFocus.getFocusReason());
            courseFocus.setRealWorldExample(aiFocus.getRealWorldExample());
            course.getCourseFocuses().add(courseFocus);
        }

        // Step 3 - build topics, each with one focus per track
        for(TopicAiResponse aiTopic : aiData.getTopics()){
            Topic topic = new Topic();
            topic.setCourse(course);
            topic.setTitle(shortTitle(aiTopic.getTitle())); // heading only, without the sub-points
            topic.setLectureHours(aiTopic.getLectureHours());
            topic.setLabHours(aiTopic.getLabHours());

            for(TopicFocusAiResponse aiTopicFocus : aiTopic.getFocuses()){
                TopicFocus topicFocus = new TopicFocus();
                topicFocus.setTopic(topic);
                topicFocus.setTargetTrack(aiTopicFocus.getTargetTrack());
                topicFocus.setFocusLevel(aiTopicFocus.getFocusLevel());
                topicFocus.setFocusReason(aiTopicFocus.getFocusReason());
                topicFocus.setRealWorldExample(aiTopicFocus.getRealWorldExample());
                topic.getFocuses().add(topicFocus);
            }
            course.getTopics().add(topic);
        }

        // Save everything at once (cascade saves the children)
        courseRepository.save(course);
        return course;
    }

    // Gemini sometimes puts the sub-points in the title, e.g.
    // "Introduction to Databases: • Types of Databases • Basic Definitions".
    // Keep only the heading: cut at the first ':', '•' or '' (PDF bullet), then trim.
    private String shortTitle(String title) {
        if (title == null) {
            return null;
        }
        int cut = title.length();
        for (char separator : new char[]{':', '•', ''}) {
            int index = title.indexOf(separator);
            if (index >= 0 && index < cut) {
                cut = index;
            }
        }
        return title.substring(0, cut).trim();
    }
}
