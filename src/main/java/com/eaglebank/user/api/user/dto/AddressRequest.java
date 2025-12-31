package com.eaglebank.user.api.user.dto;


import jakarta.validation.constraints.NotBlank;


public record AddressRequest(@NotBlank String line1, String line2, String line3, @NotBlank String town, @NotBlank String county,
									  @NotBlank String postcode) {}
