package com.anorisno.appDataTool.controller;

import com.anorisno.appDataTool.service.PhotoService;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/photo")
public class PhotoReceiverController {

    private final Logger log;

    private final PhotoService photoService;

    public PhotoReceiverController(Logger log, PhotoService photoService) {
        this.log = log;
        this.photoService = photoService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePhoto(@RequestParam("photo") MultipartFile multipartFile) {
        try(InputStream stream = multipartFile.getInputStream()) {
            photoService.savePhoto(stream);
            return ResponseEntity.ok().build();
        } catch (IOException e){
            log.error("cant save photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("can't save photo");
        }
    }
}
