package com.ziyad.courselens.domain.dto;


import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.Track;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTrackProgramRequest {

    @NotNull
    private Track track;

    @NotNull
    private Program program;

}
