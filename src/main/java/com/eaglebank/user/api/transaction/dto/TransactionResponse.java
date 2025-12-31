package com.eaglebank.user.api.transaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public record TransactionResponse(String id, String accountNumber, BigDecimal amount, String currency, String type,
											 String reference, OffsetDateTime createdTimestamp)
{
}
