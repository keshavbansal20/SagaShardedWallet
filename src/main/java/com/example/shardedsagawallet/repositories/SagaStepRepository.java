package com.example.shardedsagawallet.repositories;

import com.example.shardedsagawallet.entities.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.shardedsagawallet.entities.SagaStep;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep,Long> {

    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId , StepStatus status);

    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId , String stepName , StepStatus status);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = com.example.shardedsagawallet.entities.StepStatus.COMPLETED")
    List<SagaStep> findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status IN ( com.example.shardedsagawallet.entities.StepStatus.COMPLETED, com.example.shardedsagawallet.entities.StepStatus.COMPENSATED)")
    List<SagaStep> findCompletedOrCompensatedBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

}
