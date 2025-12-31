package com.eaglebank.user.api;

import com.eaglebank.user.api.account.dto.AccountType;
import com.eaglebank.user.api.account.dto.BankAccountResponse;
import com.eaglebank.user.api.account.dto.CreateBankAccountRequest;
import com.eaglebank.user.api.common.dto.Currency;
import com.eaglebank.user.api.transaction.dto.CreateTransactionRequest;
import com.eaglebank.user.api.transaction.dto.TransactionResponse;
import com.eaglebank.user.api.transaction.dto.TransactionType;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


public class TransactionFlowIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;


	@Test
	void deposit_then_withdraw_insufficientFunds_returns422()
	{
		HttpHeaders json = new HttpHeaders();
		json.setContentType(MediaType.APPLICATION_JSON);

		// 1) Create user
		String email = "tx+" + System.currentTimeMillis() + "@example.com";
		CreateUserRequest userReq = new CreateUserRequest("Tx User",
				new AddressRequest("1 Street", null, null, "London", "London", "AA1 1AA"), "+447700900555", email);

		UserResponse user = restTemplate.exchange("/v1/users", HttpMethod.POST, new HttpEntity<>(userReq, json), UserResponse.class)
				.getBody();

		// 2) Register + login
		restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(new RegisterRequest(email, "Password123!"), json),
				Void.class);

		LoginResponse login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(new LoginRequest(email, "Password123!"), json), LoginResponse.class).getBody();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(login.token());

		// 3) Create account
		BankAccountResponse account = restTemplate.exchange("/v1/accounts", HttpMethod.POST,
				new HttpEntity<>(new CreateBankAccountRequest("My Account", AccountType.personal), httpHeaders),
				BankAccountResponse.class).getBody();

		String accountNumber = account.accountNumber();

		// 4) Deposit £100
		CreateTransactionRequest deposit = new CreateTransactionRequest(TransactionType.deposit, new BigDecimal("100.00"),
				Currency.GBP, "initial deposit");

		ResponseEntity<TransactionResponse> depositRes = restTemplate.exchange("/v1/accounts/" + accountNumber + "/transactions",
				HttpMethod.POST, new HttpEntity<>(deposit, httpHeaders), TransactionResponse.class);

		assertThat(depositRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		// 5) Withdraw £200 → 422
		CreateTransactionRequest withdrawTooMuch = new CreateTransactionRequest(TransactionType.withdrawal,
				new BigDecimal("200.00"), Currency.GBP, "too much");

		ResponseEntity<String> withdrawRes = restTemplate.exchange("/v1/accounts/" + accountNumber + "/transactions",
				HttpMethod.POST, new HttpEntity<>(withdrawTooMuch, httpHeaders), String.class);

		assertThat(withdrawRes.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
	}

	// Local records for auth endpoints
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
