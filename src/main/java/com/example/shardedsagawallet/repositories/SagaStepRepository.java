package com.example.shardedsagawallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shardedsagawallet.entities.SagaStep;
import com.example.shardedsagawallet.entities.StepStatus;

import java.util.List;

public interface SagaStepRepository extends JpaRepository<SagaStep,Long> {

    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId , StepStatus status);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status =='COMPLETED")
    List<SagaStep> findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status IN { 'COMPLETED','COMPENSATED'}")
    List<SagaStep> findCompletedOrCompensatedBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

}
