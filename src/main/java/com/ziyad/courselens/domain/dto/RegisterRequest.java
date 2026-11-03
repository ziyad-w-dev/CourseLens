package com.ziyad.courselens.domain.dto;

import com.ziyad.courselens.domain.entity.Program;
import com.ziyad.courselens.domain.entity.Track;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Track track;

    @NotNull
    private Program program;

}
