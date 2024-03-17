package com.anorisno.appDataTool.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;


//todo add path variable to all points
@RestController
@RequestMapping(value = "/imu/experiment/{experimentId}/data")
public class ExperimentDataIMUController {

    @GetMapping()
    public ResponseEntity<Boolean> getAll() {
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Boolean> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(true);
    }

    @PostMapping
    public ResponseEntity<Boolean> create(@NonNull @RequestBody String body) {
        return ResponseEntity.ok(true);
    }

    @PutMapping
    public ResponseEntity<Boolean> update(@NonNull @RequestBody String body) {
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable("id") String id) {
        return ResponseEntity.ok(true);
    }





}
