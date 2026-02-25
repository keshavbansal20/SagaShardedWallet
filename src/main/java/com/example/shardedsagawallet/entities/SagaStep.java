package com.example.shardedsagawallet.entities;


import jakarta.persistence.Column;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="saga_step")
public class SagaStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="saga_instance_id" , nullable = false)
    private Long sagaInstanceId;


    @Column(name="step_name" , nullable = false)
    private String stepName;
    
    @Column(name="status",nullable = false)
    private StepStatus status;

    @Column(name="error_message",nullable = false)
    private String errorMessage;

    @Column(name="step_data" , columnDefinition = "json")
    private String stepData;


}
