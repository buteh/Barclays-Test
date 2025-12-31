package com.eaglebank.user.api.auth.controller;

import com.eaglebank.user.api.auth.dto.UserRegistrationRequest;
import com.eaglebank.user.api.auth.jwt.JwtService;
import com.eaglebank.user.api.auth.service.AuthenticationService;
import com.eaglebank.user.api.user.dto.LoginRequest;
import com.eaglebank.user.api.user.dto.LoginResponse;
import com.eaglebank.user.api.user.persistence.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController
{
	private final AuthenticationService authService;
	private final JwtService jwtService;

	public AuthController(AuthenticationService authService, JwtService jwtService)
	{
		this.authService = authService;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request)
	{
		UserEntity user = authService.authenticate(request.email(), request.password());
		String token = jwtService.issueToken(user.getId());
		return new LoginResponse(token);
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void register(@RequestBody @Valid UserRegistrationRequest registrationRequest)
	{
		authService.register(registrationRequest.email(), registrationRequest.password());
	}

}
