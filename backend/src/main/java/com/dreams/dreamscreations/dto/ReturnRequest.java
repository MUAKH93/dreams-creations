package com.dreams.dreamscreations.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReturnRequest {
    private Integer returnedOk;
    private Integer damaged;
    private Integer missing;
    private List<SkuLineReturnRequest> skuLines;

    @Data
    public static class SkuLineReturnRequest {
        private Long lineId;
        private Integer returnedOk;
        private Integer damaged;
        private Integer missing;
    }
}
