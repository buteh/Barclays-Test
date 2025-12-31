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

import static org.assertj.core.api.Assertions.assertThat;


public class AuthenticationFlowIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void register_then_login_returnsToken_and_token_allowsProtectedAccess()
	{
		HttpHeaders json = new HttpHeaders();
		json.setContentType(MediaType.APPLICATION_JSON);

		// 1) Create user
		CreateUserRequest create = new CreateUserRequest("Auth User",
				new AddressRequest("1 High Street", null, null, "London", "Greater London", "SW1A 1AA"), "+447700900123",
				"auth.user2@example.com");

		ResponseEntity<UserResponse> created = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(create, json),
				UserResponse.class);

		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(created.getBody()).isNotNull();

		String userId = created.getBody().id();

		// 2) Register credentials
		UserRegistrationRequest register = new UserRegistrationRequest("auth.user2@example.com", "Password123!");

		ResponseEntity<Void> registered = restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(register, json),
				Void.class);

		assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// 3) Login
		LoginRequest loginReq = new LoginRequest("auth.user2@example.com", "Password123!");

		ResponseEntity<LoginResponse> login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(loginReq, json), LoginResponse.class);

		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(login.getBody()).isNotNull();
		assertThat(login.getBody().token()).isNotBlank();

		String token = login.getBody().token();

		// 4) Call protected endpoint WITH token -> 200
		HttpHeaders authHeaders = new HttpHeaders();
		authHeaders.set("Authorization", "Bearer " + token);

		ResponseEntity<UserResponse> fetched = restTemplate.exchange("/v1/users/" + userId, HttpMethod.GET,
				new HttpEntity<>(authHeaders), UserResponse.class);

		assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(fetched.getBody()).isNotNull();
		assertThat(fetched.getBody().id()).isEqualTo(userId);

		// 5) Call protected endpoint WITHOUT token -> 401
		ResponseEntity<String> noToken = restTemplate.exchange("/v1/users/" + userId, HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()), String.class);

		assertThat(noToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void accessingAnotherUsersData_returns403()
	{
		HttpHeaders json = new HttpHeaders();
		json.setContentType(MediaType.APPLICATION_JSON);

		// Create user A
		String emailA = "userA+" + System.currentTimeMillis() + "@example.com";
		CreateUserRequest userA = new CreateUserRequest("User A",
				new AddressRequest("1 A St", null, null, "London", "London", "AA1 1AA"), "+447700900111", emailA);

		ResponseEntity<UserResponse> createdA = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(userA, json),
				UserResponse.class);

		String userAId = createdA.getBody().id();

		// Register + login user A
		restTemplate.exchange("/auth/register", HttpMethod.POST,
				new HttpEntity<>(new UserRegistrationRequest(emailA, "Password123!"), json), Void.class);

		ResponseEntity<LoginResponse> loginA = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(new LoginRequest(emailA, "Password123!"), json), LoginResponse.class);

		String tokenA = loginA.getBody().token();

		// Create user B
		String emailB = "userB+" + System.currentTimeMillis() + "@example.com";
		CreateUserRequest userB = new CreateUserRequest("User B",
				new AddressRequest("2 B St", null, null, "London", "London", "BB1 1BB"), "+447700900222", emailB);

		ResponseEntity<UserResponse> createdB = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(userB, json),
				UserResponse.class);

		String userBId = createdB.getBody().id();

		// Try to access B with A's token → 403
		HttpHeaders authHeaders = new HttpHeaders();
		authHeaders.set("Authorization", "Bearer " + tokenA);

		ResponseEntity<String> forbidden = restTemplate.exchange("/v1/users/" + userBId, HttpMethod.GET,
				new HttpEntity<>(authHeaders), String.class);

		assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	// Records to match your AuthController request/response shapes
	record LoginRequest(String email, String password)
	{
	}

	record LoginResponse(String token)
	{
	}

}
