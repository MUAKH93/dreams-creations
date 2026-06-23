package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.service.DesignImageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/design-images")
public class DesignImageController {

    @Autowired
    private DesignImageService designImageService;


    @PostMapping("/upload/{designId}")
    /*@PostMapping("/test")
    public String testUpload(
            @RequestParam("file") MultipartFile file) {

        return file.getOriginalFilename();
    }*/
    public String uploadImage(
            @PathVariable Long designId,
            @RequestParam("file") MultipartFile file) {

        designImageService.uploadImage(designId, file);
        return "Image uploaded successfully";
    }

}
