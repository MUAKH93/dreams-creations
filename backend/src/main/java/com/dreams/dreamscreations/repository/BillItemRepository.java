package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBill(Bill bill);

    @Query("SELECT bi FROM BillItem bi " +
           "JOIN FETCH bi.bill b " +
           "JOIN FETCH bi.product p " +
           "JOIN FETCH p.suit s " +
           "JOIN FETCH s.design d " +
           "WHERE b.status <> 'cancelled' AND b.billDate >= :since")
    List<BillItem> findActiveItemsSince(@Param("since") LocalDateTime since);
}
