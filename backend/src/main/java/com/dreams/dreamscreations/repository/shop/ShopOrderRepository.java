package com.dreams.dreamscreations.repository.shop;

import com.dreams.dreamscreations.entity.shop.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    Optional<ShopOrder> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM ShopOrder o JOIN FETCH o.customer c "
            + "LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product p "
            + "LEFT JOIN FETCH p.suit s LEFT JOIN FETCH s.design d LEFT JOIN FETCH s.size "
            + "ORDER BY o.createdAt DESC")
    List<ShopOrder> findAllWithDetails();

    @Query("SELECT o FROM ShopOrder o JOIN FETCH o.customer c "
            + "LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product p "
            + "LEFT JOIN FETCH p.suit s LEFT JOIN FETCH s.design d LEFT JOIN FETCH s.size "
            + "WHERE o.customer.customerId = :customerId ORDER BY o.createdAt DESC")
    List<ShopOrder> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    @Query("SELECT o FROM ShopOrder o JOIN FETCH o.customer c "
            + "LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product p "
            + "LEFT JOIN FETCH p.suit s LEFT JOIN FETCH s.design d LEFT JOIN FETCH s.size "
            + "WHERE o.orderId = :orderId")
    Optional<ShopOrder> findByIdWithDetails(@Param("orderId") Long orderId);

    @Query("SELECT o FROM ShopOrder o JOIN FETCH o.customer c "
            + "LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product p "
            + "LEFT JOIN FETCH p.suit s LEFT JOIN FETCH s.design d LEFT JOIN FETCH s.size "
            + "WHERE o.orderId = :orderId AND o.customer.customerId = :customerId")
    Optional<ShopOrder> findByIdAndCustomerIdWithDetails(@Param("orderId") Long orderId,
                                                         @Param("customerId") Long customerId);

    long countByOrderNumberStartingWith(String prefix);
}
