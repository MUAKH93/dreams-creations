package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopAnalyticsDTO;
import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.shop.ShopOrder;
import com.dreams.dreamscreations.entity.shop.ShopOrderItem;
import com.dreams.dreamscreations.repository.shop.ShopOrderRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopAnalyticsServiceImpl implements ShopAnalyticsService {

    private final ShopOrderRepository orderRepo;

    public ShopAnalyticsServiceImpl(ShopOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public ShopAnalyticsDTO getAnalytics() {
        List<ShopOrder> orders = orderRepo.findAllWithDetails();
        ShopAnalyticsDTO dto = new ShopAnalyticsDTO();
        dto.setTotalOrders(orders.size());

        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        Map<Long, ShopAnalyticsDTO.TopDesignRow> designMap = new HashMap<>();

        for (ShopOrder order : orders) {
            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "";
            switch (status) {
                case "pending" -> dto.setPendingOrders(dto.getPendingOrders() + 1);
                case "confirmed" -> dto.setConfirmedOrders(dto.getConfirmedOrders() + 1);
                case "fulfilled" -> dto.setFulfilledOrders(dto.getFulfilledOrders() + 1);
                case "cancelled" -> dto.setCancelledOrders(dto.getCancelledOrders() + 1);
                default -> { }
            }

            if ("cancelled".equals(status)) {
                continue;
            }

            BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            revenue = revenue.add(total);
            BigDecimal orderPaid = order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO;
            paid = paid.add(orderPaid);

            if (order.getItems() != null) {
                for (ShopOrderItem line : order.getItems()) {
                    if (line.getProduct() == null || line.getProduct().getSuit() == null
                            || line.getProduct().getSuit().getDesign() == null) {
                        continue;
                    }
                    Design design = line.getProduct().getSuit().getDesign();
                    Long designId = design.getDesignId();
                    ShopAnalyticsDTO.TopDesignRow row = designMap.computeIfAbsent(designId, id -> {
                        ShopAnalyticsDTO.TopDesignRow r = new ShopAnalyticsDTO.TopDesignRow();
                        r.setDesignId(designId);
                        r.setDesignCode(design.getDesignCode());
                        r.setDesignName(design.getName());
                        return r;
                    });
                    row.setQuantitySold(row.getQuantitySold() + line.getQuantity());
                    BigDecimal lineRev = line.getTotalPrice() != null ? line.getTotalPrice() : BigDecimal.ZERO;
                    row.setRevenue(row.getRevenue().add(lineRev));
                }
            }
        }

        dto.setTotalRevenue(revenue);
        dto.setTotalPaid(paid);
        dto.setOutstanding(revenue.subtract(paid).max(BigDecimal.ZERO));

        List<ShopAnalyticsDTO.TopDesignRow> top = designMap.values().stream()
                .sorted(Comparator.comparing(ShopAnalyticsDTO.TopDesignRow::getRevenue).reversed())
                .limit(10)
                .toList();
        dto.setTopDesigns(top);
        return dto;
    }
}
