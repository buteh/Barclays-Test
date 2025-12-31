package com.eaglebank.user.api.account.controller;

import com.eaglebank.user.api.account.dto.BankAccountResponse;
import com.eaglebank.user.api.account.dto.CreateBankAccountRequest;
import com.eaglebank.user.api.account.dto.GetBankAccountsResponse;
import com.eaglebank.user.api.account.dto.UpdateBankAccountRequest;
import com.eaglebank.user.api.account.persistence.AccountEntity;
import com.eaglebank.user.api.account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/accounts")
public class AccountController
{
	private final AccountService accountService;

	public AccountController(AccountService accountService)
	{
		this.accountService = accountService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BankAccountResponse createAccount(@RequestBody @Valid CreateBankAccountRequest request, Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		return accountService.createAccount(userId, request);
	}

	@GetMapping
	public GetBankAccountsResponse listAccounts(Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		return new GetBankAccountsResponse(accountService.listAccounts(userId));
	}

	@GetMapping("/{accountNumber}")
	public BankAccountResponse getAccount(@PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
			Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		return accountService.getAccount(userId, accountNumber);
	}

	@PatchMapping("/{accountNumber}")
	public BankAccountResponse update(@PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
			@RequestBody @Valid UpdateBankAccountRequest requestObject, Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		AccountEntity updated = accountService.updateAccount(userId, accountNumber, requestObject);
		return new BankAccountResponse(updated.getAccountNumber(), updated.getSortCode(), updated.getName(),
				updated.getAccountType(), updated.getBalance(), updated.getCurrency(), updated.getCreatedTimestamp(),
				updated.getUpdatedTimestamp());
	}

	@DeleteMapping("/{accountNumber}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber, Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		accountService.deleteAccount(userId, accountNumber);
	}

}


