package com.gymflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserStatsDto {
    private long total;
    private long members;
    private long trainers;
    private long staff;
    private long active;
    private long inactive;
    private List<UserDto> recentUsers;
}
