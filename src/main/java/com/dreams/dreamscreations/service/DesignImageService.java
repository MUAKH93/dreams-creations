package com.dreams.dreamscreations.service;

import org.springframework.web.multipart.MultipartFile;

public interface DesignImageService {

    public void uploadImage(Long design_id, MultipartFile file);
}
