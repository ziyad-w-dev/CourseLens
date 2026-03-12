package com.ziyad.courselens.domain.dto;


import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.Track;
import lombok.Data;

@Data
public class UpdateTrackProgramRequest {

    private Track track;
    private Program program;

}
