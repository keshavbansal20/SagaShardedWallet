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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;
    
    @Override
    @Transactional
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
    @Transactional
    public boolean executeStep(Long sagaInstanceId , String stepName){
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow( () -> new RuntimeException());

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if(step == null){
            log.error("Saga step not found for step name {}" , stepName);
            throw new RuntimeException("Saga step not found");
        }

        // SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId,StepStatus.PENDING)
        // .stream()
        // .filter(s -> s.getStepName().equals(stepName))
        // .findFirst()
        // .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());

        SagaStep sagaStepDB = sagaStepRepository
        .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId , stepName , StepStatus.PENDING)
        .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null){
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try{

            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class); 
            sagaStepDB.markAsRunning();
            sagaStepRepository.save(sagaStepDB); //update 

            boolean success = step.execute(sagaContext);

            if(success){
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB); // updating the status to completed in db

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.markAsRunning();
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully" , stepName);

            } else {

                sagaStepDB.markAsFailed();;
                sagaStepRepository.save(sagaStepDB);

                log.error("Failed to execute step {} ",stepName);

                return false;
            }

        } catch(Exception e){
            sagaStepDB.markAsFailed();;
            sagaStepRepository.save(sagaStepDB);

            log.error("Failed to execute step {}" , stepName);
            return false;
        }
        return false;
    }

    @Override

    public boolean compensateStep(Long sagaInstanceId , String stepName){
        SagaInstance sagaInstance  = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Intance not found"));


        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step==null){
            log.error("Saga step not found for step name {}" , stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName,StepStatus.COMPLETED ).orElse(null);

        if(sagaStepDB.getId()==null){
            log.info("Step {} not found in the db for saga instance {}, so it is already compensated or not executed" , stepName , sagaInstanceId);
            return true;
        }

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext() , SagaContext.class);
            sagaStepDB.markAsCompensating();
            sagaStepRepository.save(sagaStepDB); //updating the status to running in db

            boolean success = step.compensate(sagaContext);
            if(success){
                sagaStepDB.markAsCompensated();
                sagaStepRepository.save(sagaStepDB);

                log.info("Step {} compensated successfully" , stepName);
                return true;
            } else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                
                log.error("Step {} failed" , stepName);
                return false;
            }
        } catch(Exception e){
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName);
            return false;

        }
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId){
        return sagaInstanceRepository.findById(sagaInstanceId).orElseThrow( () -> new RuntimeException(" Saga instance not found"));
    }

    @Override
    public void compensateSaga(Long sagaInstanceId){
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));

        sagaInstance.maskAsCompensating();

        sagaInstanceRepository.save(sagaInstance);

        //get all the completed steps
        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated = true;
        for(SagaStep completedStep:completedSteps){
            boolean compensated =this.compensateStep(sagaInstanceId, completedStep.getStepName());
            if(!compensated){
                allCompensated = true;
            }
        }

        if(allCompensated){
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} compensated successfully",  sagaInstanceId);
        }else{
            log.error("Saga {} compensation failed");
        }

    }

    @Override
    public void failSaga(Long sagaInstanceId){
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Instance not found"));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);

    }

    @Override
    public void completeSaga(Long sagaInstanceId){
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(()-> new RuntimeException("Saga Instance not found"));

        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);

    }
}
