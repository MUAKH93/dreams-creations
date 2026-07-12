package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Quotation;

import java.util.List;

public interface QuotationService {
    Quotation create(Quotation quotation);
    Quotation update(Long id, Quotation quotation);
    List<Quotation> getAll();
    Quotation getById(Long id);
    List<Quotation> getMyQuotations();
    Quotation submit(Long id);
    Quotation updateStatus(Long id, String status);
    Quotation convertToBill(Long id);
    String generateNextQuotationNumber();
}
