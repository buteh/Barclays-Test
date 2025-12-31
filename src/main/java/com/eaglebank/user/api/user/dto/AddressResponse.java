package com.eaglebank.user.api.user.dto;


import jakarta.validation.constraints.NotBlank;


/**
 * Created a separate DTO for response because we do not need validations on response objects, but we do on the request object.
 * @param line1
 * @param line2
 * @param line3
 * @param town
 * @param county
 * @param postcode
 */
public record AddressResponse(String line1, String line2, String line3, String town, String county, String postcode)
{
}
