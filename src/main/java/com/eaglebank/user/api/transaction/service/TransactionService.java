package com.eaglebank.user.api.transaction.service;

import com.eaglebank.user.api.account.persistence.AccountEntity;
import com.eaglebank.user.api.account.persistence.AccountRepository;
import com.eaglebank.user.api.common.dto.Currency;
import com.eaglebank.user.api.common.exception.ForbiddenException;
import com.eaglebank.user.api.common.exception.NotFoundException;
import com.eaglebank.user.api.common.exception.UnprocessableEntityException;
import com.eaglebank.user.api.transaction.dto.CreateTransactionRequest;
import com.eaglebank.user.api.transaction.dto.TransactionResponse;
import com.eaglebank.user.api.transaction.persistence.TransactionEntity;
import com.eaglebank.user.api.transaction.persistence.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class TransactionService
{
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository)
	{
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public TransactionResponse create(String userId, String accountNumber, CreateTransactionRequest request)
	{
		// Lock + ownership in one go
		AccountEntity account = accountRepository.findOwnedForUpdate(accountNumber, userId).orElseGet(() -> {
			if (accountRepository.existsById(accountNumber))
			{
				throw new ForbiddenException("Access denied");
			}
			throw new NotFoundException("Account was not found");
		});

		if(!Currency.GBP.equals(request.currency()))
		{
			throw new UnprocessableEntityException("Only GBP is supported");
		}

		BigDecimal newBalance = switch (request.type())
		{
			case deposit -> account.getBalance().add(request.amount());
			case withdrawal ->
			{
				if (account.getBalance().compareTo(request.amount()) < 0)
				{
					throw new UnprocessableEntityException("Insufficient funds");
				}
				yield account.getBalance().subtract(request.amount());
			}
		};

		account.setBalance(newBalance);
		account.setUpdatedTimestamp(OffsetDateTime.now());
		accountRepository.save(account);

		TransactionEntity transaction = new TransactionEntity();
		transaction.setId(generateTransactionId());
		transaction.setAccountNumber(accountNumber);
		transaction.setAmount(request.amount());
		transaction.setCurrency(request.currency().name());
		transaction.setType(request.type().name());
		transaction.setReference(request.reference());
		transaction.setCreatedTimestamp(OffsetDateTime.now());

		TransactionEntity saved = transactionRepository.save(transaction);
		return toResponse(saved);
	}

	public List<TransactionResponse> list(String userId, String accountNumber)
	{
		// Ownership check (no lock needed)
		accountRepository.findByAccountNumberAndUserId(accountNumber, userId).orElseGet(() -> {
			if (accountRepository.existsById(accountNumber))
			{
				throw new ForbiddenException("Access denied");
			}
			throw new NotFoundException("Account was not found");
		});

		return transactionRepository.findByAccountNumberOrderByCreatedTimestampDesc(accountNumber).stream().map(this::toResponse)
				.toList();
	}

	public TransactionResponse get(String userId, String accountNumber, String transactionId)
	{
		// Ownership check
		accountRepository.findByAccountNumberAndUserId(accountNumber, userId).orElseGet(() -> {
			if (accountRepository.existsById(accountNumber))
			{
				throw new ForbiddenException("Access denied");
			}
			throw new NotFoundException("Account was not found");
		});

		TransactionEntity transaction = transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber)
				.orElseThrow(() -> new NotFoundException("Transaction was not found"));

		return toResponse(transaction);
	}

	private TransactionResponse toResponse(TransactionEntity transaction)
	{
		return new TransactionResponse(transaction.getId(), transaction.getAccountNumber(), transaction.getAmount(),
				transaction.getCurrency(), transaction.getType(), transaction.getReference(), transaction.getCreatedTimestamp());
	}

	private String generateTransactionId()
	{
		String random = UUID.randomUUID().toString().replace("-", "");
		return "tan-" + random.substring(0, 12);
	}

}
