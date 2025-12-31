package com.eaglebank.user.api;

import com.eaglebank.user.api.account.dto.AccountType;
import com.eaglebank.user.api.account.dto.BankAccountResponse;
import com.eaglebank.user.api.account.dto.CreateBankAccountRequest;
import com.eaglebank.user.api.account.dto.GetBankAccountsResponse;
import com.eaglebank.user.api.user.dto.AddressRequest;
import com.eaglebank.user.api.user.dto.CreateUserRequest;
import com.eaglebank.user.api.user.dto.UpdateUserRequest;
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

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;




public class UserControllerIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;


	@Test
	void createUser_returns201_andUserResponse()
	{
		// given
		CreateUserRequest req = new CreateUserRequest("Test User",
				new AddressRequest("1 High Street", null, null, "London", "Greater London", "SW1A 1AA"), "+447700900123",
				"test.user@example.com");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<CreateUserRequest> entity = new HttpEntity<>(req, headers);

		// when
		ResponseEntity<UserResponse> res = restTemplate.exchange("/v1/users", HttpMethod.POST, entity, UserResponse.class);

		// then
		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(res.getBody()).isNotNull();

		UserResponse body = res.getBody();
		assertThat(body.id()).startsWith("usr-");
		assertThat(body.name()).isEqualTo("Test User");
		assertThat(body.email()).isEqualTo("test.user@example.com");
		assertThat(body.phoneNumber()).isEqualTo("+447700900123");
		assertThat(body.createdTimestamp()).isNotNull();
		assertThat(body.updatedTimestamp()).isNotNull();

		// address
		assertThat(body.addressResponse()).isNotNull();
		assertThat(body.addressResponse().line1()).isEqualTo("1 High Street");
		assertThat(body.addressResponse().town()).isEqualTo("London");
		assertThat(body.addressResponse().county()).isEqualTo("Greater London");
		assertThat(body.addressResponse().postcode()).isEqualTo("SW1A 1AA");
	}

	@Test
	void update_and_delete_user_enforces_account_constraint()
	{
		HttpHeaders json = new HttpHeaders();
		json.setContentType(MediaType.APPLICATION_JSON);

		// 1) Create user
		String email = "user+" + System.currentTimeMillis() + "@example.com";
		CreateUserRequest create = new CreateUserRequest("User One",
				new AddressRequest("1 Street", null, null, "London", "London", "AA1 1AA"), "+447700901111", email);

		UserResponse created = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(create, json),
				UserResponse.class).getBody();


		assertNotNull(created);
		String userId = created.id();

		// 2) Register + login
		restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(new RegisterRequest(email, "Password123!"), json),
				Void.class);

		LoginResponse login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(new LoginRequest(email, "Password123!"), json), LoginResponse.class).getBody();

		HttpHeaders auth = new HttpHeaders();
		assertNotNull(login);
		auth.setBearerAuth(login.token());

		// 3) Update user
		UpdateUserRequest update = new UpdateUserRequest("Updated Name",
				new AddressRequest("2 Street", null, null, "London", "London", "BB1 1BB"), "+447700902222", email);

		ResponseEntity<UserResponse> updated = restTemplate.exchange("/v1/users/" + userId, HttpMethod.PATCH,
				new HttpEntity<>(update, auth), UserResponse.class);

		assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertNotNull(updated.getBody());
		assertThat(updated.getBody().name()).isEqualTo("Updated Name");

		// 4) Create account for user
		restTemplate.exchange("/v1/accounts", HttpMethod.POST,
				new HttpEntity<>(new CreateBankAccountRequest("Account", AccountType.personal), auth), String.class);

		// 5) Delete user with account → 409
		ResponseEntity<String> deleteConflict = restTemplate.exchange("/v1/users/" + userId, HttpMethod.DELETE,
				new HttpEntity<>(auth), String.class);

		assertThat(deleteConflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		// 6) Delete account
		// (list accountRecords to get accountNumber)
		GetBankAccountsResponse accountRecords = restTemplate.exchange("/v1/accounts", HttpMethod.GET, new HttpEntity<>(auth),
				GetBankAccountsResponse.class).getBody();
		if (Objects.nonNull(accountRecords) && Objects.nonNull(accountRecords.accounts()))
		{
			for (BankAccountResponse account : accountRecords.accounts())
			{
				ResponseEntity<Void> deletedAccountResponse = restTemplate.exchange("/v1/accounts/" + account.accountNumber(),
						HttpMethod.DELETE, new HttpEntity<>(auth), Void.class);
				assertThat(deletedAccountResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			}
		}

		// 7) Delete user → 204
		ResponseEntity<Void> deletedUserRecord = restTemplate.exchange("/v1/users/" + userId, HttpMethod.DELETE,
				new HttpEntity<>(auth), Void.class);

		assertThat(deletedUserRecord.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	record RegisterRequest(String email, String password)
	{
	}

	record LoginRequest(String email, String password)
	{
	}

	record LoginResponse(String token)
	{
	}

}
