package com.anorisno.appDataTool.controller;

import com.anorisno.appDataTool.controller.dto.ExperimentDTO;
import com.anorisno.appDataTool.controller.mappers.ExperimentMapper;
import com.anorisno.appDataTool.model.Experiment;
import com.anorisno.appDataTool.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/imu/experiment")
public class ExperimentIMUController {

    private final ExperimentService experimentService;
    private final ExperimentMapper experimentMapper;

    @GetMapping()
    public ResponseEntity<List<ExperimentDTO>> getAll() {
//        var result = experimentService.findAll().stream().map(experimentMapper::mapToDto).toList();
//        return ResponseEntity.ok(result);
        var result = experimentService.findAll();
        return ResponseEntity.ok(experimentMapper.mapToDto(result));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ExperimentDTO> getById(@PathVariable("id") UUID id) {
        Optional<Experiment> serviceResult = experimentService.getById(id);
        return serviceResult
                .map(experimentMapper::mapToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ExperimentDTO> create(@NonNull @RequestBody ExperimentDTO body) {
        var result = experimentService.create(experimentMapper.mapFromDto(body));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(experimentMapper.mapToDto(result));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ExperimentDTO> update(@NonNull @RequestBody ExperimentDTO body, @NonNull @PathVariable UUID id) {
        return experimentService.update(id, experimentMapper.mapFromDto(body))
                .map(experimentMapper::mapToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        experimentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
