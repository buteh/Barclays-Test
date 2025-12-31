package com.eaglebank.user.api;

import com.eaglebank.user.api.account.dto.AccountType;
import com.eaglebank.user.api.account.dto.BankAccountResponse;
import com.eaglebank.user.api.account.dto.CreateBankAccountRequest;
import com.eaglebank.user.api.account.dto.UpdateBankAccountRequest;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AccountControllerIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void create_update_delete_account_flow()
	{
		HttpHeaders json = new HttpHeaders();
		json.setContentType(MediaType.APPLICATION_JSON);

		// 1) Create user
		String email = "acct+" + System.currentTimeMillis() + "@example.com";
		CreateUserRequest userReq = new CreateUserRequest("Account User",
				new AddressRequest("1 Street", null, null, "London", "London", "AA1 1AA"), "+447700900888", email);

		UserResponse user = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(userReq, json), UserResponse.class)
				.getBody();

		// 2) Register + login
		restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(new RegisterRequest(email, "Password123!"), json),
				Void.class);

		LoginResponse login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(new LoginRequest(email, "Password123!"), json), LoginResponse.class).getBody();

		HttpHeaders auth = new HttpHeaders();
		auth.setBearerAuth(login.token());

		// 3) Create account
		BankAccountResponse created = restTemplate.exchange("/v1/accounts", HttpMethod.POST,
						new HttpEntity<>(new CreateBankAccountRequest("My Account", AccountType.personal), auth), BankAccountResponse.class)
				.getBody();

		assertThat(created).isNotNull();

		String accountNumber = created.accountNumber();

		// 4) Update account
		UpdateBankAccountRequest update = new UpdateBankAccountRequest("Updated Name", AccountType.personal);

		ResponseEntity<BankAccountResponse> updated = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.PATCH,
				new HttpEntity<>(update, auth), BankAccountResponse.class);

		assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertNotNull(updated.getBody());
		assertThat(updated.getBody().name()).isEqualTo("Updated Name");

		// 5) Delete account
		ResponseEntity<Void> deleted = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.DELETE,
				new HttpEntity<>(auth), Void.class);

		assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// 6) Fetch deleted → 404
		ResponseEntity<String> fetchDeleted = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.GET,
				new HttpEntity<>(auth), String.class);

		assertThat(fetchDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
