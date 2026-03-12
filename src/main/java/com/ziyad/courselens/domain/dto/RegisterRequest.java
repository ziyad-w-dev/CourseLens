package com.ziyad.courselens.domain.dto;

import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.Role;
import com.ziyad.courselens.domain.entity.Track;
import lombok.Data;

@Data
public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private Role role;
    private Track track;
    private Program program;

}
