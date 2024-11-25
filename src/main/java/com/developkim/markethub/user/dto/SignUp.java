package com.developkim.markethub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignUp {
    @NotBlank(message = "{validation.name.required}")
    private String name;

    @NotBlank(message = "{validation.password.required}")
    private String password;

    @NotBlank(message = "{validation.email.invalid}")
    @Email(message = "{validation.email.format}")
    private String email;
}
