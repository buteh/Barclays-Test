package com.eaglebank.user.api.account.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public record BankAccountResponse(String accountNumber, String sortCode, String name, String accountType, BigDecimal balance,
											 String currency, OffsetDateTime createdTimestamp, OffsetDateTime updatedTimestamp)
{
}
