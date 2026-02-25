package com.example.shardedsagawallet.services.saga;

import org.springframework.stereotype.Service;

import com.example.shardedsagawallet.entities.SagaInstance;
import com.example.shardedsagawallet.entities.SagaStatus;
import com.example.shardedsagawallet.entities.SagaStep;
import com.example.shardedsagawallet.entities.StepStatus;
import com.example.shardedsagawallet.repositories.SagaInstanceRepository;
import com.example.shardedsagawallet.repositories.SagaStepRepository;
import com.example.shardedsagawallet.steps.SagaStepFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;
    
    @Override
    public Long startSaga(SagaContext context){

        try {
            String contextJson = objectMapper.writeValueAsString(context); //convert the context to a json as a string
            SagaInstance sagaInstance = SagaInstance.builder()
            .context(contextJson)
            .status(SagaStatus.STARTED)
            .build();

            sagaInstanceRepository.save(sagaInstance);

            log.info("Started saga with id {}",sagaInstance.getId());

            return sagaInstance.getId();
        } catch(Exception e){
            log.error("Error starting sagag" , e);
            throw new RuntimeException("Error starting saga",e);
        }
    }


    @Override
    public boolean executeStep(Long sagaInstanceId , String stepName){
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow( () -> new RuntimeException());

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if(step == null){
            log.error("Saga step not found for step name {}" , stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId,StepStatus.PENDING)
        .stream()
        .filter(s -> s.getStepName().equals(stepName))
        .findFirst()
        .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null){
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try{

            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class); 
            sagaStepDB.setStatus(StepStatus.RUNNING);
            sagaStepRepository.save(sagaStepDB); //update 

            boolean success = step.execute(sagaContext);

            if(success){
                sagaStepDB.setStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(sagaStepDB); // updating the status to completed in db

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully" , stepName);

            } else {

                sagaStepDB.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStepDB);

                log.error("Failed to execute step {} ",stepName);

                return false;
            }

        } catch(Exception e){
            sagaStepDB.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStepDB);

            log.error("Failed to execute step {}" , stepName);
            return false;
        }
        return false;
    }

    @Override

    public boolean compensateStep(Long sagaInstanceId , String stepName){
        return false;
    }

    @Override

    public SagaInstance getSagaInstance(Long sagaInstanceId){
        return new SagaInstance();
    }

    @Override

    public void compensateSaga(Long sagaInstanceId){
        
    }

    @Override
    public void failSaga(Long sagaInstanceId){

    }

    @Override
    public void completeSaga(Long sagaInstanceId){

    }
}
