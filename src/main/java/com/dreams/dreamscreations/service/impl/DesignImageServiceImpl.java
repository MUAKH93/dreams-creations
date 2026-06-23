package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import com.dreams.dreamscreations.repository.DesignImageRepository;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.service.DesignImageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class DesignImageServiceImpl implements DesignImageService {

    @Autowired
    private DesignRepository designRepository;

    @Autowired
    private DesignImageRepository designImageRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public void uploadImage(Long design_id, MultipartFile file){

        try{

            Design design = designRepository.findById(design_id).orElseThrow(() -> new RuntimeException("Design not found"));

            File uploadDir = new File(UPLOAD_DIR);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String fileName = file.getOriginalFilename();
            String filePath = UPLOAD_DIR + fileName;

            File destination = new File(filePath);
            file.transferTo(destination);

            DesignImage image = new DesignImage();
            image.setImage_name(fileName);
            image.setImage_path(filePath);
            image.setUploaded_at(LocalDateTime.now());
            image.setDesign(design);

            designImageRepository.save(image);

        }catch(IOException e){
            //throw new RuntimeException("Image Upload Failed",e);
            e.printStackTrace();
            throw new RuntimeException("Image Upload Failed: " + e.getMessage());
        }
    }


}
