package com.eaglebank.user.api;

import com.eaglebank.user.api.account.dto.AccountType;
import com.eaglebank.user.api.account.dto.UpdateBankAccountRequest;
import com.eaglebank.user.api.account.persistence.AccountEntity;
import com.eaglebank.user.api.account.persistence.AccountRepository;
import com.eaglebank.user.api.account.service.AccountService;
import com.eaglebank.user.api.common.exception.ForbiddenException;
import com.eaglebank.user.api.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AccountServiceTest
{
	private final AccountRepository repo = mock(AccountRepository.class);
	private final AccountService service = new AccountService(repo);

	@Test
	void update_notOwned_throws403()
	{
		when(repo.findByAccountNumberAndUserId("01000001", "usr-1")).thenReturn(Optional.empty());
		when(repo.existsById("01000001")).thenReturn(true);

		assertThatThrownBy(() -> service.updateAccount("usr-1", "01000001",
				new UpdateBankAccountRequest("Name", AccountType.personal))).isInstanceOf(ForbiddenException.class);
	}

	@Test
	void update_notFound_throws404()
	{
		when(repo.findByAccountNumberAndUserId("01000001", "usr-1")).thenReturn(Optional.empty());
		when(repo.existsById("01000001")).thenReturn(false);

		assertThatThrownBy(() -> service.updateAccount("usr-1", "01000001",
				new UpdateBankAccountRequest("Name", AccountType.personal))).isInstanceOf(NotFoundException.class);
	}

	@Test
	void update_owned_updatesFields()
	{
		AccountEntity entity = new AccountEntity();
		entity.setAccountNumber("01000001");
		entity.setUserId("usr-1");
		entity.setName("Old");
		entity.setAccountType("personal");
		entity.setUpdatedTimestamp(OffsetDateTime.now());

		when(repo.findByAccountNumberAndUserId("01000001", "usr-1")).thenReturn(Optional.of(entity));
		when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

		service.updateAccount("usr-1", "01000001", new UpdateBankAccountRequest("New", AccountType.personal));

		verify(repo).save(argThat(accountEntity -> accountEntity.getName().equals("New")));
	}

}
