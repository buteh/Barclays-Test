package com.eaglebank.user.api.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserCredentialsRepository extends JpaRepository<UserCredentialsEntity, String>
{
	Optional<UserCredentialsEntity> findByUserId(String userId);
}
