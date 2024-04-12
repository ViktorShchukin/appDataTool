package com.anorisno.appDataTool.service;

import com.anorisno.appDataTool.model.ExperimentData;
import com.anorisno.appDataTool.repository.ExperimentDataRepository;
import com.anorisno.appDataTool.service.tools.ExperimentDataTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExperimentDataService {

    private final ExperimentDataRepository experimentDataRepository;
    private final ExperimentDataTool experimentDataTool;

    @Transactional
    public List<ExperimentData> getAllByExperimentId(UUID experimentId) {
        return experimentDataRepository.findAllByExperimentId(experimentId);
    }

    @Transactional
    public Optional<ExperimentData> getById(UUID experimentId, UUID id) {
        return experimentDataRepository.findById(id);
    }

    @Transactional
    public ExperimentData create(ExperimentData experimentData) {
        ExperimentData newExperimentData = experimentDataTool.create(
                experimentData.getId(),
                experimentData.getExperimentId(),
                experimentData.getSensorType(),
                experimentData.getAccx(),
                experimentData.getAccy(),
                experimentData.getAccz(),
                experimentData.getValueTimestamp()
        );
        return experimentDataRepository.save(newExperimentData);
    }

    @Transactional
    public Optional<ExperimentData> update(UUID id, ExperimentData experimentData) {
        return experimentDataRepository.findById(id)
                .map(experimentDataOld -> experimentDataTool.update(experimentDataOld, experimentData))
                .map(experimentDataRepository::save);
    }

    @Transactional
    public void delete(UUID id) {
        experimentDataRepository.deleteById(id);
    }
}
