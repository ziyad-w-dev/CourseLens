package com.ziyad.courselens.ai.service;

import com.ziyad.courselens.ai.dto.CourseAiResponse;
import com.ziyad.courselens.ai.dto.CourseFocusAiResponse;
import com.ziyad.courselens.ai.dto.TopicAiResponse;
import com.ziyad.courselens.ai.dto.TopicFocusAiResponse;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.common.exception.AiUnavailableException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CourseResponseValidator {

    private void checkTrack(List<Track> tracks, String where){
        Set<Track> trackSet = new HashSet<>(tracks);
        if(!(trackSet.size() == tracks.size() && trackSet.size() == Track.values().length)){
            throw new AiUnavailableException("Gemini " + where + " must contain each track exactly once");
        }
    }

    public void validate(CourseAiResponse response){
        //Check 1
        List<CourseFocusAiResponse> focuses = response.getCourseFocus();
        List<Track> courseTracks = new ArrayList<>();
        for(CourseFocusAiResponse focus : focuses){
            courseTracks.add(focus.getTargetTrack());
        }
        checkTrack(courseTracks,"Course");

        //Check 2
        List<TopicAiResponse> topicAiResponses = response.getTopics();
        for(TopicAiResponse aiResponse : topicAiResponses){
            List<TopicFocusAiResponse> topicFocusAiResponses = aiResponse.getFocuses();
            List<Track> topicTracks = new ArrayList<>();
            for(TopicFocusAiResponse track : topicFocusAiResponses){
                topicTracks.add(track.getTargetTrack());
            }
            checkTrack(topicTracks,"Topic: "+ aiResponse.getTitle());
        }

        //Check 3 & 4
        List<String> titles = new ArrayList<>();
        for(TopicAiResponse topicTitle : topicAiResponses){
            String nameOfTheTitle = topicTitle.getTitle();
            titles.add(nameOfTheTitle);
            if (nameOfTheTitle.length() > 500) {
                throw new AiUnavailableException("Gemini topic title longer than 500 chars: " + nameOfTheTitle);
            }
        }
        Set<String> titlesName = new HashSet<>(titles);
        if(titles.size() != titlesName.size()){
            throw new AiUnavailableException("Gemini returned duplicate topic titles");
        }
    }
}