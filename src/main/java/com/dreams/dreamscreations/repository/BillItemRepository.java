package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBill(Bill bill);
}
