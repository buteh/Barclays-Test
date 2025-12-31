package com.eaglebank.user.api.user.controller;


import com.eaglebank.user.api.common.exception.ForbiddenException;
import com.eaglebank.user.api.user.dto.AddressRequest;
import com.eaglebank.user.api.user.dto.AddressResponse;
import com.eaglebank.user.api.user.dto.CreateUserRequest;
import com.eaglebank.user.api.user.dto.UpdateUserRequest;
import com.eaglebank.user.api.user.dto.UserResponse;
import com.eaglebank.user.api.user.persistence.UserEntity;
import com.eaglebank.user.api.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RestController
@RequestMapping("/v1/users")
public class UserController
{
	private final UserService userService;

	public UserController(UserService userService)
	{
		this.userService = userService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody CreateUserRequest request)
	{

		UserEntity user = new UserEntity();
		user.setName(request.name());
		AddressRequest addressRequest = request.address();
		user.setLine1(addressRequest.line1());
		user.setLine2(addressRequest.line2());
		user.setLine3(addressRequest.line3());
		user.setTown(addressRequest.town());
		user.setCounty(addressRequest.county());
		user.setPostcode(addressRequest.postcode());
		user.setPhoneNumber(request.phoneNumber());
		user.setEmail(request.email());

		UserEntity saved = userService.createUser(user);

		return mapToResponse(saved);
	}

	@GetMapping("/{userId}")
	public UserResponse fetchUserById(@PathVariable @Pattern(regexp = "^usr-[A-Za-z0-9]+$") String userId)
	{
		String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!userId.equals(authenticatedUserId))
		{
			throw new ForbiddenException("Access denied");
		}
		UserEntity user = userService.getUserById(userId);
		return mapToResponse(user);
	}

	@PatchMapping("/{userId}")
	public UserResponse update(@PathVariable String userId, @RequestBody @Valid UpdateUserRequest request,
			Authentication authentication)
	{
		String authUserId = (String) authentication.getPrincipal();
		UserEntity updated = userService.updateUser(authUserId, userId, request);
		return mapToResponse(updated);
	}

	@DeleteMapping("/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String userId, Authentication authentication)
	{
		String authUserId = (String) authentication.getPrincipal();
		userService.deleteUser(authUserId, userId);
	}

	private UserResponse mapToResponse(UserEntity user)
	{
		return new UserResponse(user.getId(), user.getName(),
				new AddressResponse(user.getLine1(), user.getLine2(), user.getLine3(), user.getTown(), user.getCounty(),
						user.getPostcode()), user.getPhoneNumber(), user.getEmail(), user.getCreatedTimestamp(),
				user.getUpdatedTimestamp());
	}

}
