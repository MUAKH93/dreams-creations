package com.dreams.dreamscreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationListDTO {
    private Long quotationId;
    private String quotationNumber;
    private Long customerId;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
    private int itemCount;
}
