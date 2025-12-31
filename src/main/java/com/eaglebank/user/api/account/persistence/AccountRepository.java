package com.eaglebank.user.api.account.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface AccountRepository extends JpaRepository<AccountEntity, String>
{
	List<AccountEntity> findByUserId(String userId);

	Optional<AccountEntity> findByAccountNumberAndUserId(String accountNumber, String userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from AccountEntity a where a.accountNumber = :accountNumber and a.userId = :userId")
	Optional<AccountEntity> findOwnedForUpdate(String accountNumber, String userId);

	boolean existsById(String accountNumber);

	boolean existsByUserId(String userId);

}
