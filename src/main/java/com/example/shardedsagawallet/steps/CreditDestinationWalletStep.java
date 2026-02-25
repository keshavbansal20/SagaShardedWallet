package com.example.shardedsagawallet.steps;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.shardedsagawallet.repositories.WalletRepository;
import com.example.shardedsagawallet.services.saga.SagaContext;
import com.example.shardedsagawallet.services.saga.SagaStepInterface;
import com.example.shardedsagawallet.steps.SagaStepFactory.SagaStepType;

import jakarta.transaction.Transactional;

import com.example.shardedsagawallet.entities.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStepInterface {
    
    private WalletRepository  walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context){
// Step 1: Get the destination wallet id from the context

        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount {}",toWalletId,amount);

        //step2 fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
        .orElseThrow(()->new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {} ",wallet.getBalance());
        context.put("orginalToWalletBalance",wallet.getBalance());

        //step3 :credit the destination wallet
        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet save with balance {}",wallet.getBalance());

        //step 4 udpate the context

        context.put("toWalletBalanceAfterCredit ",wallet.getBalance());

        log.info("Credit destintaiton wallet step executed successfully");

        return true;

    }


    @Override
    @Transactional
    public boolean compensate(SagaContext context){

        //step 1 : get the destination wallet id from the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensation credit of destination Wallet {} with the amount {}",toWalletId,amount);

        //step2 : fetch the destination wallet for db wth lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
        .orElseThrow( () -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetch with balance {}",wallet.getBalance());

        //step3  credit  the destination wallet
        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet fetched with balance{}" , wallet.getBalance());

        //step4 update sagacontext;
        context.put("toWalletBalanceAfterCreditCompensation",wallet.getBalance());


        log.info("Credit compensation of destination wallet step executed successfully",wallet.getBalance());
        return false;
    };

    public String getStepName(){
        return SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    };

}
