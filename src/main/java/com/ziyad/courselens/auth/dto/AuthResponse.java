package com.ziyad.courselens.auth.dto;

import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Role role;
    private Track track;
    private String fullName;
    private Program program;

}