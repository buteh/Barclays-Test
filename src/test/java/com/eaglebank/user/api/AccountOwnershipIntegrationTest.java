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


public class AccountOwnershipIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void userB_cannot_access_userA_account_returns403()
	{
		// Create + login User A
		String emailA = uniqueEmail("owner");
		String tokenA = createUser_register_login(emailA, "Password123!");

		// Create + login User B
		String emailB = uniqueEmail("intruder");
		String tokenB = createUser_register_login(emailB, "Password123!");

		// User A creates an account
		String accountNumber = createAccount(tokenA, "Owner Account", AccountType.personal);

		// User B tries to GET -> 403
		ResponseEntity<String> getAsB = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.GET,
				new HttpEntity<>(authHeaders(tokenB)), String.class);
		assertThat(getAsB.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

		// User B tries to PATCH -> 403
		UpdateBankAccountRequest patch = new UpdateBankAccountRequest("Hacked", AccountType.personal);
		ResponseEntity<String> patchAsB = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.PATCH,
				new HttpEntity<>(patch, authHeaders(tokenB)), String.class);
		assertThat(patchAsB.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

		// User B tries to DELETE -> 403
		ResponseEntity<String> deleteAsB = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.DELETE,
				new HttpEntity<>(authHeaders(tokenB)), String.class);
		assertThat(deleteAsB.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	private String createUser_register_login(String email, String password)
	{
		HttpHeaders json = new HttpHeaders();
		json.setContentType(MediaType.APPLICATION_JSON);

		CreateUserRequest create = new CreateUserRequest("Test " + email,
				new AddressRequest("1 High Street", null, null, "London", "Greater London", "SW1A 1AA"),
				"+4477009" + (int) (Math.random() * 1000000), email);

		ResponseEntity<UserResponse> created = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(create, json),
				UserResponse.class);
		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		// register credentials
		ResponseEntity<Void> registered = restTemplate.exchange("/auth/register", HttpMethod.POST,
				new HttpEntity<>(new RegisterRequest(email, password), json), Void.class);
		assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// login
		ResponseEntity<LoginResponse> login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(new LoginRequest(email, password), json), LoginResponse.class);
		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(login.getBody()).isNotNull();
		assertThat(login.getBody().token()).isNotBlank();

		return login.getBody().token();
	}

	private String createAccount(String token, String name, AccountType type)
	{
		ResponseEntity<BankAccountResponse> res = restTemplate.exchange("/v1/accounts", HttpMethod.POST,
				new HttpEntity<>(new CreateBankAccountRequest(name, type), authHeaders(token)), BankAccountResponse.class);
		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(res.getBody()).isNotNull();
		return res.getBody().accountNumber();
	}

	private HttpHeaders authHeaders(String token)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private String uniqueEmail(String prefix)
	{
		return prefix + "+" + System.currentTimeMillis() + "@example.com";
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
