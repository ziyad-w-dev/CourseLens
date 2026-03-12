package com.ziyad.courselens.domain.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;

}