package com.eaglebank.user.api.transaction.dto;

import java.util.List;


public record GetTransactionsResponse(List<TransactionResponse> transactions)
{
}
