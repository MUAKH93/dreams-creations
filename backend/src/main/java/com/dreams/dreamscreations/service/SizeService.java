package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Size;
import java.util.List;

public interface SizeService {
    Size saveSize(Size size);
    List<Size> getAllSizes();
    Size getSizeById(Long id);
    List<Size> getSizesByCategoryId(Long categoryId);
}
