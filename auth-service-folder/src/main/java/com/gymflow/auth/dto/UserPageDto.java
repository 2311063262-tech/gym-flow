package com.gymflow.auth.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record UserPageDto(
        List<UserDto> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last
) {
    public static UserPageDto from(Page<UserDto> page) {
        return new UserPageDto(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}
