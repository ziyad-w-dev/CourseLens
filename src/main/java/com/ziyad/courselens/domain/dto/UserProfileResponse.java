package com.ziyad.courselens.domain.dto;


import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.Role;
import com.ziyad.courselens.domain.entity.Track;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private Track track;
    private Program program;
    private LocalDateTime createdAt;

}
