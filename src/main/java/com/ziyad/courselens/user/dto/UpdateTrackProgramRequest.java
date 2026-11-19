package com.ziyad.courselens.user.dto;


import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTrackProgramRequest {

    @NotNull
    private Track track;

    @NotNull
    private Program program;

}
