package com.dreams.dreamscreations.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DispatchRequest {
    private Long batchId;
    private Long moduleId;
    private Long supervisorId;
    private Integer quantitySent;
    private LocalDateTime dueDate;
    private Long designingWorkTypeId;
    private Long fillingWorkTypeId;
    private List<SkuLineRequest> skuLines;

    @Data
    public static class SkuLineRequest {
        private Long sizeId;
        private String color;
        private Integer quantity;
    }
}
