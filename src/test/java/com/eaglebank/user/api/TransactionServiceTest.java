package com.eaglebank.user.api;

import com.eaglebank.user.api.account.persistence.AccountEntity;
import com.eaglebank.user.api.account.persistence.AccountRepository;
import com.eaglebank.user.api.common.dto.Currency;
import com.eaglebank.user.api.common.exception.UnprocessableEntityException;
import com.eaglebank.user.api.transaction.dto.CreateTransactionRequest;
import com.eaglebank.user.api.transaction.dto.TransactionType;
import com.eaglebank.user.api.transaction.persistence.TransactionRepository;
import com.eaglebank.user.api.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TransactionServiceTest
{
	private final AccountRepository accountRepository = mock(AccountRepository.class);
	private final TransactionRepository transactionRepository = mock(TransactionRepository.class);

	private final TransactionService service = new TransactionService(accountRepository, transactionRepository);

	@Test
	void withdrawal_moreThanBalance_throws422()
	{
		AccountEntity account = new AccountEntity();
		account.setAccountNumber("01000001");
		account.setUserId("usr-1");
		account.setBalance(new BigDecimal("50.00"));
		account.setCurrency("GBP");
		account.setCreatedTimestamp(OffsetDateTime.now());
		account.setUpdatedTimestamp(OffsetDateTime.now());

		when(accountRepository.findOwnedForUpdate("01000001", "usr-1")).thenReturn(Optional.of(account));

		CreateTransactionRequest req = new CreateTransactionRequest(TransactionType.withdrawal, new BigDecimal("100.00"),
				Currency.GBP, null);

		assertThatThrownBy(() -> service.create("usr-1", "01000001", req)).isInstanceOf(UnprocessableEntityException.class);
	}

}
