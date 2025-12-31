package com.eaglebank.user.api.user.dto;

import java.time.OffsetDateTime;


public record UserResponse(String id, String name, AddressResponse addressResponse, String phoneNumber, String email,
									OffsetDateTime createdTimestamp, OffsetDateTime updatedTimestamp) {}

