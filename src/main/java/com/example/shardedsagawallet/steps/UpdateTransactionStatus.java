package com.example.shardedsagawallet.steps;

import org.springframework.stereotype.Service;

import com.example.shardedsagawallet.entities.Transaction;
import com.example.shardedsagawallet.entities.TransactionStatus;
import com.example.shardedsagawallet.repositories.TransactionRepository;
import com.example.shardedsagawallet.services.saga.SagaContext;
import com.example.shardedsagawallet.services.saga.SagaStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStep {
    
    private final TransactionRepository transactionRepository;

    @Override
    public  boolean execute(SagaContext context){
        Long transactionId = context.getLong("transactionId");

        log.info("Updating transaction status for transaction {}" , transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow( () -> new RuntimeException("Transactio not found"));

        context.put("orginalTransactionStatus",transaction.getStatus());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for tranaction {}",transactionId);

        context.put("transactionStatusAfterUpdate",transaction.getStatus());

        log.info("Update transaction status step executed successfully");

        return true;
    }


    @Override
    public boolean compensate(SagaContext context){

        Long transactionId = context.getLong("transactionId");

        TransactionStatus originalTransactionStatus = TransactionStatus.valueOf(context.getString("originalTransactionStatus"));

        log.info("Compensating transactio status for transaction {} ",transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow( () -> new RuntimeException("Transaction not found"));

        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);
        
        return true;
    }

    @Override
    public String getStepName(){
        return "UpdateTransactionStatus";
    }

}
