package com.gymflow.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MembershipValidationResponse {
    private Boolean valid;
    private String status;
    private LocalDate endDate;
    private String message;
}
