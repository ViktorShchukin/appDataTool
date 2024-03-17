package com.anorisno.appDataTool.controller;

import com.anorisno.appDataTool.model.Experiment;
import com.anorisno.appDataTool.service.ExperimentService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/umi/experiment")
public class ExperimentIMUController {

    private final ExperimentService experimentService;

    public ExperimentIMUController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping()
    public ResponseEntity<List<Experiment>> getAll() {
        return ResponseEntity.ok(experimentService.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(id);
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
