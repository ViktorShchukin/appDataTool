package com.anorisno.appDataTool.controller;

import com.anorisno.appDataTool.controller.dto.ExperimentDataDTO;
import com.anorisno.appDataTool.controller.mappers.ExperimentDataMapper;
import com.anorisno.appDataTool.service.ExperimentDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


//@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/imu/experiment/{experimentId}/data")
public class ExperimentDataIMUController {

    private final ExperimentDataMapper experimentDataMapper;
    private final ExperimentDataService experimentDataService;

    public ExperimentDataIMUController(ExperimentDataMapper experimentDataMapper, ExperimentDataService experimentDataService) {
        this.experimentDataMapper = experimentDataMapper;
        this.experimentDataService = experimentDataService;
    }

    @GetMapping()
    public ResponseEntity<List<ExperimentDataDTO>> getAll(@NonNull @PathVariable("experimentId") UUID experimentId) {
        var result = experimentDataService.getAllByExperimentId(experimentId);
        return ResponseEntity.ok(experimentDataMapper.mapToDTO(result));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ExperimentDataDTO> getById(@NonNull @PathVariable("experimentId") UUID experimentId,
                                                     @NonNull @PathVariable("id") UUID id) {
        return experimentDataService.getById(experimentId, id)
                .map(experimentDataMapper::mapToDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ExperimentDataDTO> create(@NonNull @PathVariable("experimentId") UUID experimentId,
                                                    @NonNull @RequestBody ExperimentDataDTO body) {
        var result = experimentDataService.create(experimentDataMapper.fromDTO(body));
        return ResponseEntity.ok(experimentDataMapper.mapToDTO(result));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ExperimentDataDTO> update(@NonNull @PathVariable("experimentId") UUID experimentId,
                                                    @NonNull @PathVariable("id") UUID id,
                                                    @NonNull @RequestBody ExperimentDataDTO body) {
        return experimentDataService.update(id, experimentDataMapper.fromDTO(body))
                .map(experimentDataMapper::mapToDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@NonNull @PathVariable("experimentId") UUID experimentId,
                                    @NonNull @PathVariable("id") UUID id) {
        experimentDataService.delete(id);
        return ResponseEntity.noContent().build();
    }





}
