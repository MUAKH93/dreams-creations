package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.service.DesignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesignServiceImpl implements DesignService {

    @Autowired
    private final DesignRepository designRepository;

    public DesignServiceImpl(DesignRepository designRepository){
        this.designRepository = designRepository;
    }

    @Override
    public Design saveDesign(Design design) {
        return designRepository.save(design);  // ✅ correct
    }

    @Override
    public List<Design> getAllDesigns(){
        return designRepository.findAll();
    }
}
