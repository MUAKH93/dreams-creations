package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Design;

import java.util.List;

public interface DesignService {

    Design saveDesign(Design design);

    List<Design> getAllDesigns();
}
