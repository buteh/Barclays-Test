package com.eaglebank.user.api;

import com.eaglebank.user.api.account.persistence.AccountRepository;
import com.eaglebank.user.api.common.exception.ConflictException;
import com.eaglebank.user.api.common.exception.ForbiddenException;
import com.eaglebank.user.api.common.exception.NotFoundException;
import com.eaglebank.user.api.user.dto.AddressRequest;
import com.eaglebank.user.api.user.dto.UpdateUserRequest;
import com.eaglebank.user.api.user.persistence.UserEntity;
import com.eaglebank.user.api.user.persistence.UserRepository;
import com.eaglebank.user.api.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UserServiceTest
{

	private final UserRepository userRepository = mock(UserRepository.class);
	private final AccountRepository accountRepository = mock(AccountRepository.class);
	private final UserService userService = new UserService(userRepository, accountRepository);

	@Test
	void createUser_whenEmailExists_throwsConflict()
	{
		UserEntity user = new UserEntity();
		user.setEmail("a@b.com");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(new UserEntity()));

		assertThatThrownBy(() -> userService.createUser(user)).isInstanceOf(ConflictException.class)
				.hasMessageContaining("Email already in use");

		verify(userRepository, never()).save(any());
	}

	@Test
	void createUser_setsIdAndTimestamps_andSaves()
	{
		UserEntity user = new UserEntity();
		user.setEmail("a@b.com");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		UserEntity saved = userService.createUser(user);

		assertThat(saved.getId()).startsWith("usr-");
		assertThat(saved.getId().length()).isEqualTo(4 + 12);
		assertThat(saved.getCreatedTimestamp()).isNotNull();
		assertThat(saved.getUpdatedTimestamp()).isNotNull();
		assertThat(saved.getCreatedTimestamp()).isEqualTo(saved.getUpdatedTimestamp());

		verify(userRepository).save(any(UserEntity.class));
	}

	@Test
	void createUser_whenSaveThrowsDataIntegrityViolation_throwsConflict()
	{
		UserEntity user = new UserEntity();
		user.setEmail("a@b.com");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException("dup"));

		assertThatThrownBy(() -> userService.createUser(user)).isInstanceOf(ConflictException.class)
				.hasMessageContaining("Email already in use");
	}

	@Test
	void getUserById_whenMissing_throwsNotFound()
	{
		when(userRepository.findById("usr-xxx")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUserById("usr-xxx")).isInstanceOf(NotFoundException.class)
				.hasMessageContaining("User was not found");
	}

	@Test
	void update_notOwner_throws403()
	{
		UpdateUserRequest req = new UpdateUserRequest("Name", new AddressRequest("1", null, null, "Town", "County", "AA"),
				"+447700900000", "a@b.com");

		assertThatThrownBy(() -> userService.updateUser("usr-1", "usr-2", req)).isInstanceOf(ForbiddenException.class);
	}

	@Test
	void delete_withAccounts_throws409()
	{
		when(userRepository.existsById("usr-1")).thenReturn(true);
		when(accountRepository.existsByUserId("usr-1")).thenReturn(true);

		assertThatThrownBy(() -> userService.deleteUser("usr-1", "usr-1")).isInstanceOf(ConflictException.class);
	}

}
