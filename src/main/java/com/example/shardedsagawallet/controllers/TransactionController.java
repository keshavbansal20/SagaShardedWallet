package com.example.shardedsagawallet.controllers;

import org.apache.calcite.avatica.remote.Service.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shardedsagawallet.dtos.TransferRequestDTO;
import com.example.shardedsagawallet.dtos.TransferResponseDTO;
import com.example.shardedsagawallet.services.TransferSagaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/transactions")
public class TransactionController {
    
    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTransaction(@RequestBody TransferRequestDTO transferRequestDTO){
        
        try{
            Long sagaInstanceId = transferSagaService.initiateTransfer(transferRequestDTO.getFromWalleteId(),transferRequestDTO.getToWalletId() , transferRequestDTO.getAmount(), transferRequestDTO.getDescription());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                TransferResponseDTO.builder()
                    .sagaInstanceId(sagaInstanceId)
                    .build()
            );
        } catch(Exception e){
            log.error("Error creating transaction",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
