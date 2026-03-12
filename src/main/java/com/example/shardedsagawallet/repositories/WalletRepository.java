package com.example.shardedsagawallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;


import com.example.shardedsagawallet.entities.Wallet;

import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {
    
    List<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) //many type of locks in mysql
    @Query("SELECT w from Wallet w WHERE w.userId = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") Long id);

    
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = :balance WHERE w.userId = :userId")
    void updateBalanceByUserId(@Param("userId") Long userId , @Param("balance") BigDecimal balance);

}
