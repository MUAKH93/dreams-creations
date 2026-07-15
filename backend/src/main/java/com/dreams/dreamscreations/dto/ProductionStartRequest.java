package com.dreams.dreamscreations.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    /** Multiple designs in one order — each line creates its own batch. */
    private List<DesignBatchLine> designLines;

    @Data
    public static class DesignBatchLine {
        private Long designId;
        private Integer quantity;
        private String color;
        private String designLabel;
        private String articleName;
    }
}
