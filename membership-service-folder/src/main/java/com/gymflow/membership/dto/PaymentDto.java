package com.gymflow.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long membershipId;
    private Double amount;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
