package com.mundomanitas.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperacionRequest(@NotBlank @Email String email) {
}
