package com.eaglebank.user.api.transaction.controller;

import com.eaglebank.user.api.transaction.dto.CreateTransactionRequest;
import com.eaglebank.user.api.transaction.dto.GetTransactionsResponse;
import com.eaglebank.user.api.transaction.dto.TransactionResponse;
import com.eaglebank.user.api.transaction.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController
{
	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TransactionResponse create(@PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
			@RequestBody @Valid CreateTransactionRequest request, Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		return transactionService.create(userId, accountNumber, request);
	}

	@GetMapping
	public GetTransactionsResponse list(@PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
			Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		return new GetTransactionsResponse(transactionService.list(userId, accountNumber));
	}

	@GetMapping("/{transactionId}")
	public TransactionResponse get(@PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
			@PathVariable String transactionId, Authentication authentication)
	{
		String userId = (String) authentication.getPrincipal();
		return transactionService.get(userId, accountNumber, transactionId);
	}

}
