package com.eaglebank.user.api.transaction.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TransactionRepository extends JpaRepository<TransactionEntity, String>
{
	List<TransactionEntity> findByAccountNumberOrderByCreatedTimestampDesc(String accountNumber);

	Optional<TransactionEntity> findByIdAndAccountNumber(String id, String accountNumber);

}
