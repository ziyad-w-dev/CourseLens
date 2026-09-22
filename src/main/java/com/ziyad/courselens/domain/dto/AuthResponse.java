package com.ziyad.courselens.domain.dto;


import com.ziyad.courselens.domain.entity.Role;
import com.ziyad.courselens.domain.entity.Track;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Role role;
    private Track track;

}