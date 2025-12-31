package com.eaglebank.user.api;

import com.eaglebank.user.api.auth.dto.UserRegistrationRequest;
import com.eaglebank.user.api.user.dto.AddressRequest;
import com.eaglebank.user.api.user.dto.CreateUserRequest;
import com.eaglebank.user.api.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class AuthControllerIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void login_returnsToken_and_token_allowsAccessToProtectedEndpoint() throws Exception
	{
		// 1) Create a user
		CreateUserRequest req = new CreateUserRequest("Auth User",
				new AddressRequest("10 Downing St", null, null, "London", "Greater London", "SW1A 2AA"), "+447700900999",
				"auth.user@example.com");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<UserResponse> created = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(req, headers),
				UserResponse.class);

		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(created.getBody()).isNotNull();

		String userId = created.getBody().id();

		// 2) Inserting credentials row for the user (since signup does not set password)
		UserRegistrationRequest registerReq = new UserRegistrationRequest("auth.user@example.com", "password");

		ResponseEntity<Void> registered = restTemplate.exchange("/auth/register", HttpMethod.POST,
				new HttpEntity<>(registerReq, headers), Void.class);

		assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


		// 3) Login
		LoginRequest loginReq = new LoginRequest("auth.user@example.com", "password");

		ResponseEntity<LoginResponse> login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(loginReq, headers), LoginResponse.class);

		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(login.getBody()).isNotNull();
		assertThat(login.getBody().token()).isNotBlank();

		String token = login.getBody().token();

		// 4) Call protected endpoint with token
		HttpHeaders authHeaders = new HttpHeaders();
		authHeaders.set("Authorization", "Bearer " + token);

		ResponseEntity<UserResponse> fetched = restTemplate.exchange("/v1/users/" + userId, HttpMethod.GET,
				new HttpEntity<>(authHeaders), UserResponse.class);

		assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(fetched.getBody()).isNotNull();
		assertThat(fetched.getBody().id()).isEqualTo(userId);
	}

	// Keep request/response records local to the test to match your AuthController
	record LoginRequest(String email, String password)
	{
	}

	record LoginResponse(String token)
	{
	}

}
