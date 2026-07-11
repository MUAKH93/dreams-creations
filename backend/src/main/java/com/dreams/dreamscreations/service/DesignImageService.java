package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.DesignImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DesignImageService {

    DesignImage uploadImage(Long designId, MultipartFile file);

    List<DesignImage> getImagesByDesignId(Long designId);

    void deleteImage(Long imageId);

    DesignImage updateImage(Long imageId, MultipartFile file);
}
