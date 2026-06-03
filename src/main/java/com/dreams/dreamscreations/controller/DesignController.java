package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.service.DesignService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService){
        this.designService = designService;
    }

    @PostMapping
    public Design createDesign(@RequestBody Design design){
        return designService.saveDesign(design);
    }

    @GetMapping
    public List<Design> getAllDesigns(){
        return designService.getAllDesigns();
    }

}
