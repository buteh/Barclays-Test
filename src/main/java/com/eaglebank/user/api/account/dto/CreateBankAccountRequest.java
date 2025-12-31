package com.eaglebank.user.api.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CreateBankAccountRequest(@NotBlank String name, @NotNull AccountType accountType)
{
}
