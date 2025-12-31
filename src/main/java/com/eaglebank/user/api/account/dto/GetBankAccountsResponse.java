package com.eaglebank.user.api.account.dto;

import java.util.List;


public record GetBankAccountsResponse(List<BankAccountResponse> accounts)
{
}
