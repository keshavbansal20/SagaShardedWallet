package com.example.shardedsagawallet.controllers;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shardedsagawallet.dtos.CreateWalletRequestDTO;
import com.example.shardedsagawallet.dtos.CreditWalletRequestDTO;
import com.example.shardedsagawallet.dtos.DebitWalletRequestDTO;
import com.example.shardedsagawallet.entities.Wallet;
import com.example.shardedsagawallet.services.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallets")
@Slf4j
public class WalletController{
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody CreateWalletRequestDTO request){
        try {
            Wallet newWallet = walletService.createWallet(request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
        } catch (Exception e){
            log.error("Error creating wallet",e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id){
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }


    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id){
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long userId ,@RequestBody DebitWalletRequestDTO request ){

        try {
            walletService.debit(userId , request.getAmount());
            Wallet wallet = walletService.getWalletByUserId(userId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e){
            log.info("debit failed with userId {}", userId );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long userId , @RequestBody CreditWalletRequestDTO request){
        walletService.credit(userId , request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
}