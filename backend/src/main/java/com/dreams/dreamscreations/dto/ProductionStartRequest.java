package com.dreams.dreamscreations.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionStartRequest {
    private Long designId;
    private Long categoryId;
    private Long sizeId;
    private String color;
    private Integer quantity;
    private LocalDate expectedCompletionDate;
    private LocalDateTime dueDate;
    private Long supervisorId;
    private Long designingWorkTypeId;
}
