package com.ziyad.courselens.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

}