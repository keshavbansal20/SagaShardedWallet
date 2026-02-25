package com.example.shardedsagawallet.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.shardedsagawallet.entities.Transaction;
import org.springframework.transaction.TransactionStatus;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    
    List<Transaction> findByFromWalletId(Long fromWalletId);

    List<Transaction> findByToWalletId(Long toWalletId);

    @Query("SELECT t from transaction t where t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(Long walletId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);

}
