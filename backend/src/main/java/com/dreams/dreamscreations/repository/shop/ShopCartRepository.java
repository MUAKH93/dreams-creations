package com.dreams.dreamscreations.repository.shop;

import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.shop.ShopCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopCartRepository extends JpaRepository<ShopCart, Long> {

    @Query("SELECT c FROM ShopCart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product p "
            + "LEFT JOIN FETCH p.suit s LEFT JOIN FETCH s.design d LEFT JOIN FETCH d.images "
            + "LEFT JOIN FETCH s.size WHERE c.customer.customerId = :customerId")
    Optional<ShopCart> findByCustomerIdWithItems(@Param("customerId") Long customerId);

    Optional<ShopCart> findByCustomer(Customer customer);
}
