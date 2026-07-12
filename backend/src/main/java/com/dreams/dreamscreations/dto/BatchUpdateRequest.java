package com.dreams.dreamscreations.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchUpdateRequest {
    private Integer totalSuitPlanned;
    private LocalDate expectedCompletionDate;
}
