package com.ziyad.courselens.domain.dto;

import com.ziyad.courselens.domain.entity.Track;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackDistributionResponse {

    private Track track;
    private Long studentCount;

}
