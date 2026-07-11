package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.repository.SuitRepository;
import com.dreams.dreamscreations.service.SuitService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SuitServiceImpl implements SuitService {

    private final SuitRepository suitRepo;
    private final DesignRepository designRepo;

    public SuitServiceImpl(SuitRepository suitRepo, DesignRepository designRepo) {
        this.suitRepo = suitRepo;
        this.designRepo = designRepo;
    }

    @Override public Suit save(Suit suit) { return suitRepo.save(suit); }
    @Override public List<Suit> getAll() { return suitRepo.findAll(); }

    @Override
    public Suit getById(Long id) {
        return suitRepo.findById(id).orElseThrow(() -> new RuntimeException("Suit not found: " + id));
    }

    @Override
    public List<Suit> getByDesignId(Long designId) {
        Design design = designRepo.findById(designId)
                .orElseThrow(() -> new RuntimeException("Design not found: " + designId));
        return suitRepo.findByDesign(design);
    }

    @Override
    public Suit update(Long id, Suit updated) {
        Suit existing = getById(id);
        existing.setDesign(updated.getDesign());
        existing.setSize(updated.getSize());
        existing.setColor(updated.getColor());
        existing.setStatus(updated.getStatus());
        return suitRepo.save(existing);
    }

    @Override public void delete(Long id) { suitRepo.delete(getById(id)); }
}
