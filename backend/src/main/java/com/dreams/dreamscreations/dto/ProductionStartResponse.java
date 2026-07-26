package com.dreams.dreamscreations.dto;

import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionStartResponse {
    private ProductionBatch batch;
    private Suit suit;
    private ModuleAssignment assignment;
    private List<StartedBatchItem> startedBatches;
    private String message;

    @Data
    @AllArgsConstructor
    public static class StartedBatchItem {
        private ProductionBatch batch;
        private Suit suit;
        private ModuleAssignment assignment;
    }
}
