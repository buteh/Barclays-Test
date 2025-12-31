package com.eaglebank.user.api.user.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;


@Entity
@Table(name = "user_credentials")
public class UserCredentialsEntity
{
	@Id
	@Column(name = "user_id", nullable = false, length = 50)
	private String userId;

	@Column(name = "password_hash", nullable = false, length = 200)
	private String passwordHash;

	@Column(name = "created_timestamp", nullable = false)
	private OffsetDateTime createdTimestamp;

	@Column(name = "updated_timestamp", nullable = false)
	private OffsetDateTime updatedTimestamp;

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash)
	{
		this.passwordHash = passwordHash;
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
