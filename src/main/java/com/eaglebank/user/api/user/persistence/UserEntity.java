package com.eaglebank.user.api.user.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;


@Entity
@Table(name = "users")
public class UserEntity
{

	@Id
	@Column(name = "id", nullable = false, length = 50)
	private String id; // Not using UUID because of the specified prefix(usr-) requirement in the OpenAI documentation.

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	// Address (flattened as per V1__init.sql)
	@Column(name = "line1", nullable = false, length = 200)
	private String line1;

	@Column(name = "line2", length = 200)
	private String line2;

	@Column(name = "line3", length = 200)
	private String line3;

	@Column(name = "town", nullable = false, length = 100)
	private String town;

	@Column(name = "county", nullable = false, length = 100)
	private String county;

	@Column(name = "postcode", nullable = false, length = 20)
	private String postcode;

	@Column(name = "phone_number", nullable = false, length = 20)
	private String phoneNumber;

	@Column(name = "email", nullable = false, length = 200)
	private String email;

	@Column(name = "created_timestamp", nullable = false)
	private OffsetDateTime createdTimestamp;

	@Column(name = "updated_timestamp", nullable = false)
	private OffsetDateTime updatedTimestamp;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLine1()
	{
		return line1;
	}

	public void setLine1(String line1)
	{
		this.line1 = line1;
	}

	public String getLine2()
	{
		return line2;
	}

	public void setLine2(String line2)
	{
		this.line2 = line2;
	}

	public String getLine3()
	{
		return line3;
	}

	public void setLine3(String line3)
	{
		this.line3 = line3;
	}

	public String getTown()
	{
		return town;
	}

	public void setTown(String town)
	{
		this.town = town;
	}

	public String getCounty()
	{
		return county;
	}

	public void setCounty(String county)
	{
		this.county = county;
	}

	public String getPostcode()
	{
		return postcode;
	}

	public void setPostcode(String postcode)
	{
		this.postcode = postcode;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
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