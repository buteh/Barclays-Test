package com.eaglebank.user.api.transaction.dto;

import com.eaglebank.user.api.common.dto.Currency;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


public record CreateTransactionRequest(@NotNull TransactionType type,
													@NotNull @DecimalMin(value = "0.00", inclusive = true) @DecimalMax(value = "10000.00", inclusive = true) BigDecimal amount,
													@NotNull Currency currency, @Size(max = 200) String reference)
{
}
