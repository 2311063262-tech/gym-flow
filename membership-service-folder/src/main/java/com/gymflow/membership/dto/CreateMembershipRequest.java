package com.gymflow.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMembershipRequest {
    @NotNull(message = "Member ID is required")
    private Long memberId;
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
}
