package com.eaglebank.user.api;

import com.eaglebank.user.api.auth.service.AuthenticationService;
import com.eaglebank.user.api.common.exception.UnauthorizedException;
import com.eaglebank.user.api.user.persistence.UserCredentialsEntity;
import com.eaglebank.user.api.user.persistence.UserCredentialsRepository;
import com.eaglebank.user.api.user.persistence.UserEntity;
import com.eaglebank.user.api.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AuthenticationServiceTest
{
	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserCredentialsRepository credentialsRepository = mock(UserCredentialsRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

	private final AuthenticationService authService = new AuthenticationService(userRepository, credentialsRepository,
			passwordEncoder);

	@Test
	void authenticate_whenUserMissing_throwsUnauthorized()
	{
		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.authenticate("a@b.com", "pw")).isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("Invalid credentials");
	}

	@Test
	void authenticate_whenCredentialsMissing_throwsUnauthorized()
	{
		UserEntity user = new UserEntity();
		user.setId("usr-123");
		user.setEmail("a@b.com");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
		when(credentialsRepository.findByUserId("usr-123")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.authenticate("a@b.com", "pw")).isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("Invalid credentials");
	}

	@Test
	void authenticate_whenPasswordWrong_throwsUnauthorized()
	{
		UserEntity user = new UserEntity();
		user.setId("usr-123");
		user.setEmail("a@b.com");

		UserCredentialsEntity creds = new UserCredentialsEntity();
		creds.setUserId("usr-123");
		creds.setPasswordHash("hash");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
		when(credentialsRepository.findByUserId("usr-123")).thenReturn(Optional.of(creds));
		when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

		assertThatThrownBy(() -> authService.authenticate("a@b.com", "bad")).isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("Invalid credentials");
	}

	@Test
	void authenticate_whenValid_returnsUser()
	{
		UserEntity user = new UserEntity();
		user.setId("usr-123");
		user.setEmail("a@b.com");

		UserCredentialsEntity creds = new UserCredentialsEntity();
		creds.setUserId("usr-123");
		creds.setPasswordHash("hash");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
		when(credentialsRepository.findByUserId("usr-123")).thenReturn(Optional.of(creds));
		when(passwordEncoder.matches("pw", "hash")).thenReturn(true);

		UserEntity result = authService.authenticate("a@b.com", "pw");

		assertThat(result.getId()).isEqualTo("usr-123");
	}

	@Test
	void register_whenUserMissing_throwsUnauthorized()
	{
		when(userRepository.findByEmail("missing@b.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.register("missing@b.com", "pw")).isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("Invalid credentials");

		verify(credentialsRepository, never()).save(any());
	}

	@Test
	void register_savesCredentials_withEncodedPassword()
	{
		UserEntity user = new UserEntity();
		user.setId("usr-123");
		user.setEmail("a@b.com");

		when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
		when(credentialsRepository.findByUserId("usr-123")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("pw")).thenReturn("encoded");

		authService.register("a@b.com", "pw");

		verify(credentialsRepository).save(argThat(
				saved -> saved.getUserId().equals("usr-123") && saved.getPasswordHash().equals("encoded")
						&& saved.getCreatedTimestamp() != null && saved.getUpdatedTimestamp() != null));
	}



}
