package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Bill;
import java.util.List;

public interface BillService {
    Bill createBill(Bill bill);
    List<Bill> getAll();
    Bill getById(Long id);
    List<Bill> getByCustomerId(Long customerId);
    List<Bill> getMyBills();
    List<Bill> getByStatus(String status);
    Bill updateStatus(Long id, String status);
    String generateNextBillNumber();
}
