package com.dreams.dreamscreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardChartsDTO {

    private List<MonthlySalesPoint> salesByMonth;
    private Map<String, Long> productionPipeline;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlySalesPoint {
        private String month;
        private BigDecimal amount;
    }
}
