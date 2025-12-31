package com.eaglebank.user.api.user.service;

import com.eaglebank.user.api.account.persistence.AccountRepository;
import com.eaglebank.user.api.common.exception.ConflictException;
import com.eaglebank.user.api.common.exception.ForbiddenException;
import com.eaglebank.user.api.common.exception.NotFoundException;
import com.eaglebank.user.api.user.dto.UpdateUserRequest;
import com.eaglebank.user.api.user.persistence.UserEntity;
import com.eaglebank.user.api.user.persistence.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
public class UserService
{
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;

	public UserService(UserRepository userRepository, AccountRepository accountRepository)
	{
		this.userRepository = userRepository;
		this.accountRepository = accountRepository;
	}

	@Transactional
	public UserEntity createUser(UserEntity user)
	{

		if (userRepository.findByEmail(user.getEmail()).isPresent())
		{
			throw new ConflictException("Email already in use");
		}

		OffsetDateTime now = OffsetDateTime.now();
		user.setId(generateUserId());
		user.setCreatedTimestamp(now);
		user.setUpdatedTimestamp(now);

		try
		{
			return userRepository.save(user);
		}
		catch (DataIntegrityViolationException exception)
		{
			throw new ConflictException("Email already in use");
		}
	}

	public UserEntity getUserById(String userId)
	{
		return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User was not found"));
	}

	@Transactional
	public UserEntity updateUser(String authUserId, String userId, UpdateUserRequest req)
	{
		if (!authUserId.equals(userId))
		{
			throw new ForbiddenException("Access denied");
		}

		UserEntity user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User was not found"));

		user.setName(req.name());
		user.setLine1(req.address().line1());
		user.setLine2(req.address().line2());
		user.setLine3(req.address().line3());
		user.setTown(req.address().town());
		user.setCounty(req.address().county());
		user.setPostcode(req.address().postcode());
		user.setPhoneNumber(req.phoneNumber());
		user.setEmail(req.email());
		user.setUpdatedTimestamp(OffsetDateTime.now());

		try
		{
			return userRepository.save(user);
		}
		catch (DataIntegrityViolationException e)
		{
			throw new ConflictException("Email already in use");
		}
	}

	@Transactional
	public void deleteUser(String authUserId, String userId)
	{
		if (!authUserId.equals(userId))
		{
			throw new ForbiddenException("Access denied");
		}

		if (!userRepository.existsById(userId))
		{
			throw new NotFoundException("User was not found");
		}

		if (accountRepository.existsByUserId(userId))
		{
			throw new ConflictException("User has active accounts");
		}

		userRepository.deleteById(userId);
	}


	private String generateUserId()
	{
		String random = UUID.randomUUID().toString().replace("-", "");
		return "usr-" + random.substring(0, 12);
	}

}
