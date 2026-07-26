package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Suit;
import java.util.List;

public interface SuitService {
    Suit save(Suit suit);
    List<Suit> getAll();
    Suit getById(Long id);
    List<Suit> getByDesignId(Long designId);
    Suit update(Long id, Suit suit);
    void delete(Long id);
}
