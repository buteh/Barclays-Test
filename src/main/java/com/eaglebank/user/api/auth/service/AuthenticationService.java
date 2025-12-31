package com.eaglebank.user.api.auth.service;

import com.eaglebank.user.api.common.exception.UnauthorizedException;
import com.eaglebank.user.api.user.persistence.UserCredentialsEntity;
import com.eaglebank.user.api.user.persistence.UserCredentialsRepository;
import com.eaglebank.user.api.user.persistence.UserEntity;
import com.eaglebank.user.api.user.persistence.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;


@Service
public class AuthenticationService
{
	private final UserRepository userRepository;
	private final UserCredentialsRepository credentialsRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthenticationService(UserRepository userRepository, UserCredentialsRepository credentialsRepository,
			PasswordEncoder passwordEncoder)
	{
		this.userRepository = userRepository;
		this.credentialsRepository = credentialsRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UserEntity authenticate(String email, String rawPassword)
	{

		UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

		UserCredentialsEntity creds = credentialsRepository.findByUserId(user.getId())
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

		if (!passwordEncoder.matches(rawPassword, creds.getPasswordHash()))
		{
			throw new UnauthorizedException("Invalid credentials");
		}

		return user;
	}

	public void register(@Email @NotBlank String email, @NotBlank String password)
	{
		UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

		String hash = passwordEncoder.encode(password);

		UserCredentialsEntity creds = credentialsRepository.findByUserId(user.getId()).orElseGet(UserCredentialsEntity::new);

		creds.setUserId(user.getId());
		creds.setPasswordHash(hash);
		creds.setCreatedTimestamp(OffsetDateTime.now());
		creds.setUpdatedTimestamp(OffsetDateTime.now());

		credentialsRepository.save(creds);
	}

}
