package com.ziyad.courselens.user.dto;


import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.user.entity.Role;
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
