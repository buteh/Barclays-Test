package com.eaglebank.user.api.transaction.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Table(name = "transactions")
public class TransactionEntity
{
	@Id
	@Column(name = "id", length = 50, nullable = false)
	private String id;

	@Column(name = "account_number", length = 8, nullable = false)
	private String accountNumber;

	@Column(name = "amount", precision = 12, scale = 2, nullable = false)
	private BigDecimal amount;

	@Column(name = "currency", length = 3, nullable = false)
	private String currency;

	@Column(name = "type", length = 20, nullable = false)
	private String type;

	@Column(name = "reference", length = 200)
	private String reference;

	@Column(name = "created_timestamp", nullable = false)
	private OffsetDateTime createdTimestamp;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String reference)
	{
		this.reference = reference;
	}

	public OffsetDateTime getCreatedTimestamp()
	{
		return createdTimestamp;
	}

	public void setCreatedTimestamp(OffsetDateTime createdTimestamp)
	{
		this.createdTimestamp = createdTimestamp;
	}

}
