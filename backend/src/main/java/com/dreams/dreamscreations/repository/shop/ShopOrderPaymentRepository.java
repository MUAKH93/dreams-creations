package com.dreams.dreamscreations.repository.shop;

import com.dreams.dreamscreations.entity.shop.ShopOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ShopOrderPaymentRepository extends JpaRepository<ShopOrderPayment, Long> {

    List<ShopOrderPayment> findByOrder_OrderIdOrderByCreatedAtDesc(Long orderId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM ShopOrderPayment p WHERE p.order.orderId = :orderId")
    BigDecimal sumByOrderId(@Param("orderId") Long orderId);
}
