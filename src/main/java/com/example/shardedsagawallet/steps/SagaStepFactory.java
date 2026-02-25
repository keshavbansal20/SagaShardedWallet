package com.example.shardedsagawallet.steps;

import com.example.shardedsagawallet.services.saga.SagaStepInterface;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {
    
    private final Map<String , SagaStepInterface> sagaStepMap;

    public static enum SagaStepType {
        DEBIT_SOURCE_WALLET_STEP , 
        CREDIT_DESTINATION_WALLET_STEP , 
        UDPATE_TRANSACTION_STATUS_STEP
    }

    public SagaStepInterface getSagaStep(String stepName){
        return sagaStepMap.get(stepName);
    }


}
