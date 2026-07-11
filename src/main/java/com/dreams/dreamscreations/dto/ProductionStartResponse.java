package com.dreams.dreamscreations.dto;

import com.dreams.dreamscreations.entity.ModuleAssignment;
import com.dreams.dreamscreations.entity.ProductionBatch;
import com.dreams.dreamscreations.entity.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductionStartResponse {
    private ProductionBatch batch;
    private Suit suit;
    private ModuleAssignment assignment;
    private String message;
}
