package com.eaglebank.user.api.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record CreateUserRequest(@NotBlank String name, @Valid @NotNull AddressRequest address,
										  @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,
										  @Email @NotBlank String email) {}

