package com.eaglebank.user.api.account.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Table(name = "accounts")
public class AccountEntity
{
	@Id
	@Column(name = "account_number", length = 8, nullable = false)
	private String accountNumber;

	@Column(name = "sort_code", length = 8, nullable = false)
	private String sortCode;

	@Column(name = "name", length = 200, nullable = false)
	private String name;

	@Column(name = "account_type", length = 20, nullable = false)
	private String accountType;

	@Column(name = "balance", precision = 12, scale = 2, nullable = false)
	private BigDecimal balance;

	@Column(name = "currency", length = 3, nullable = false)
	private String currency;

	@Column(name = "user_id", length = 50, nullable = false)
	private String userId;

	@Column(name = "created_timestamp", nullable = false)
	private OffsetDateTime createdTimestamp;

	@Column(name = "updated_timestamp", nullable = false)
	private OffsetDateTime updatedTimestamp;

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public String getSortCode()
	{
		return sortCode;
	}

	public void setSortCode(String sortCode)
	{
		this.sortCode = sortCode;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAccountType()
	{
		return accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public BigDecimal getBalance()
	{
		return balance;
	}

	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public OffsetDateTime getCreatedTimestamp()
	{
		return createdTimestamp;
	}

	public void setCreatedTimestamp(OffsetDateTime createdTimestamp)
	{
		this.createdTimestamp = createdTimestamp;
	}

	public OffsetDateTime getUpdatedTimestamp()
	{
		return updatedTimestamp;
	}

	public void setUpdatedTimestamp(OffsetDateTime updatedTimestamp)
	{
		this.updatedTimestamp = updatedTimestamp;
	}

}
