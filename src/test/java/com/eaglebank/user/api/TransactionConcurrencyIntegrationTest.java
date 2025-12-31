package com.eaglebank.user.api;

import com.eaglebank.user.api.account.dto.AccountType;
import com.eaglebank.user.api.account.dto.BankAccountResponse;
import com.eaglebank.user.api.account.dto.CreateBankAccountRequest;
import com.eaglebank.user.api.common.dto.Currency;
import com.eaglebank.user.api.transaction.dto.CreateTransactionRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


public class TransactionConcurrencyIntegrationTest extends IntegrationTestBase
{
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void two_withdrawals_in_parallel_only_one_succeeds_other_returns422() throws Exception
	{
		String email = uniqueEmail("concurrency");
		String token = createUser_register_login(email, "Password123!");

		String accountNumber = createAccount(token, "Concurrency Account", AccountType.personal);

		// Deposit 100
		ResponseEntity<String> deposit = restTemplate.exchange("/v1/accounts/" + accountNumber + "/transactions", HttpMethod.POST,
				new HttpEntity<>(
						new CreateTransactionRequest(TransactionType.deposit, new BigDecimal("100.00"), Currency.GBP, "seed"),
						authHeaders(token)), String.class);
		assertThat(deposit.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		// Two withdrawals of 70 in parallel
		ExecutorService pool = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Callable<HttpStatus> withdrawCall = () -> {
			ready.countDown();
			start.await(5, TimeUnit.SECONDS);

			ResponseEntity<String> respond = restTemplate.exchange("/v1/accounts/" + accountNumber + "/transactions",
					HttpMethod.POST, new HttpEntity<>(
							new CreateTransactionRequest(TransactionType.withdrawal, new BigDecimal("70.00"), Currency.GBP, "race"),
							authHeaders(token)), String.class);
			return HttpStatus.valueOf(respond.getStatusCode().value());
		};

		Future<HttpStatus> f1 = pool.submit(withdrawCall);
		Future<HttpStatus> f2 = pool.submit(withdrawCall);

		assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
		start.countDown();

		HttpStatus status1 = f1.get(10, TimeUnit.SECONDS);
		HttpStatus status2 = f2.get(10, TimeUnit.SECONDS);

		pool.shutdownNow();

		List<HttpStatus> statuses = new ArrayList<>(List.of(status1, status2));
		statuses.sort(HttpStatus::compareTo);

		// Expect one 201 and one 422
		assertThat(statuses).containsExactly(HttpStatus.CREATED, HttpStatus.UNPROCESSABLE_ENTITY);

		// Final balance should be 30.00
		ResponseEntity<BankAccountResponse> account = restTemplate.exchange("/v1/accounts/" + accountNumber, HttpMethod.GET,
				new HttpEntity<>(authHeaders(token)), BankAccountResponse.class);
		assertThat(account.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(account.getBody()).isNotNull();
		assertThat(account.getBody().balance()).isEqualByComparingTo(new BigDecimal("30.00"));
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

		ResponseEntity<Void> registered = restTemplate.exchange("/auth/register", HttpMethod.POST,
				new HttpEntity<>(new RegisterRequest(email, password), json), Void.class);
		assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<LoginResponse> login = restTemplate.exchange("/auth/login", HttpMethod.POST,
				new HttpEntity<>(new LoginRequest(email, password), json), LoginResponse.class);
		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(login.getBody()).isNotNull();
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
		HttpHeaders h = new HttpHeaders();
		h.setBearerAuth(token);
		h.setContentType(MediaType.APPLICATION_JSON);
		return h;
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
