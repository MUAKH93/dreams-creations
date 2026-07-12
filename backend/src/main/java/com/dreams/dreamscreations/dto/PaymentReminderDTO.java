package com.dreams.dreamscreations.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentReminderDTO {
    private Long customerId;
    private String customerName;
    private String phone;
    private BigDecimal balanceDue;
    private long overdueBillCount;
    private LocalDateTime oldestBillDate;
    private int daysOverdue;
}
