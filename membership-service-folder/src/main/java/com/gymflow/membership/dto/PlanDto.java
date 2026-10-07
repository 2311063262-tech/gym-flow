package com.gymflow.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PlanDto {
    private Long id;
    private String name;
    private Double price;
    private Integer duration;
    private List<String> features;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
