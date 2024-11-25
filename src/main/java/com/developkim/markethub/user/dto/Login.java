package com.developkim.markethub.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class Login {

    @NotBlank(message = "{validation.name.required}")
    private String name;

    @NotBlank(message = "{validation.password.required}")
    private String password;
}
