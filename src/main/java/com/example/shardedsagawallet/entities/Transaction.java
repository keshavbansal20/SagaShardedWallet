package com.example.shardedsagawallet.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_wallet_id",nullable = false)
    private Long fromWalletId;

    @Column(name="to_wallet_id",nullable = false)
    private Long toWalletId;

    @Column(name="amount",nullable = false)
    private BigDecimal amount;

    @Column(name="transaction_status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name="description",nullable = false)
    private String description;

    @Column(name="saga_instance_id",nullable = false)
    private Long sagaInstanceId;
}
