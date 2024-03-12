package com.anorisno.appDataTool.controller;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/imu")
public class DataIMUController {

    @GetMapping(value = "/test")
    public ResponseEntity<Boolean> testResponse() {
        return ResponseEntity.ok(true);
    }
}
