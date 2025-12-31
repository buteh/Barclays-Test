package com.eaglebank.user.api.account.service;

import com.eaglebank.user.api.account.dto.BankAccountResponse;
import com.eaglebank.user.api.account.dto.CreateBankAccountRequest;
import com.eaglebank.user.api.account.dto.UpdateBankAccountRequest;
import com.eaglebank.user.api.account.persistence.AccountEntity;
import com.eaglebank.user.api.account.persistence.AccountRepository;
import com.eaglebank.user.api.common.dto.Currency;
import com.eaglebank.user.api.common.exception.ForbiddenException;
import com.eaglebank.user.api.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class AccountService
{
	private static final String SORT_CODE = "10-10-10";

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository)
	{
		this.accountRepository = accountRepository;
	}

	@Transactional
	public BankAccountResponse createAccount(String userId, CreateBankAccountRequest req)
	{
		OffsetDateTime now = OffsetDateTime.now();

		AccountEntity entity = new AccountEntity();
		entity.setAccountNumber(generateAccountNumber());
		entity.setSortCode(SORT_CODE);
		entity.setName(req.name());
		entity.setAccountType(req.accountType().name());
		entity.setBalance(BigDecimal.ZERO);
		entity.setCurrency(Currency.GBP.name());
		entity.setUserId(userId);
		entity.setCreatedTimestamp(now);
		entity.setUpdatedTimestamp(now);

		AccountEntity saved = accountRepository.save(entity);
		return toResponse(saved);
	}

	public List<BankAccountResponse> listAccounts(String userId)
	{
		return accountRepository.findByUserId(userId).stream().map(this::toResponse).toList();
	}

	public BankAccountResponse getAccount(String userId, String accountNumber)
	{
		return accountRepository.findByAccountNumberAndUserId(accountNumber, userId).map(this::toResponse).orElseGet(() -> {
			// Then: exists but not owned => 403, else 404
			if (accountRepository.existsById(accountNumber))
			{
				throw new ForbiddenException("Access denied");
			}
			throw new NotFoundException("Account was not found");
		});
	}

	@Transactional
	public AccountEntity updateAccount(String userId, String accountNumber, UpdateBankAccountRequest request)
	{
		AccountEntity account = accountRepository.findByAccountNumberAndUserId(accountNumber, userId).orElseGet(() -> {
			if (accountRepository.existsById(accountNumber))
			{
				throw new ForbiddenException("Access denied");
			}
			throw new NotFoundException("Bank account was not found");
		});

		account.setName(request.name());
		account.setAccountType(request.accountType().name());
		account.setUpdatedTimestamp(OffsetDateTime.now());

		return accountRepository.save(account);
	}

	@Transactional
	public void deleteAccount(String userId, String accountNumber)
	{
		AccountEntity account = accountRepository.findByAccountNumberAndUserId(accountNumber, userId).orElseGet(() -> {
			if (accountRepository.existsById(accountNumber))
			{
				throw new ForbiddenException("Access denied");
			}
			throw new NotFoundException("Bank account was not found");
		});

		accountRepository.delete(account);
	}

	private BankAccountResponse toResponse(AccountEntity accountEntity)
	{
		return new BankAccountResponse(accountEntity.getAccountNumber(), accountEntity.getSortCode(), accountEntity.getName(),
				accountEntity.getAccountType(), accountEntity.getBalance(), accountEntity.getCurrency(),
				accountEntity.getCreatedTimestamp(), accountEntity.getUpdatedTimestamp());
	}

	private String generateAccountNumber()
	{
		// Must match ^01\\d{6}$ (8 chars total)
		for (int i = 0; i < 25; i++)
		{
			int sixDigits = ThreadLocalRandom.current().nextInt(0, 1_000_000);
			String candidate = "01" + String.format("%06d", sixDigits);
			if (!accountRepository.existsById(candidate))
			{
				return candidate;
			}
		}
		throw new IllegalStateException("Could not allocate unique account number");
	}

}
