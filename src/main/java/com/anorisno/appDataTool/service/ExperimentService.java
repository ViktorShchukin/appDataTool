package com.anorisno.appDataTool.service;


import com.anorisno.appDataTool.model.Experiment;
import com.anorisno.appDataTool.model.ExperimentData;
import com.anorisno.appDataTool.repository.ExperimentRepository;
import com.anorisno.appDataTool.service.tools.ExperimentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ExperimentTool experimentTool;
    private final ExperimentDataService experimentDataService;



    @Transactional
    public List<Experiment> findAll() {return experimentRepository.findAll();
    }

    public Optional<Experiment> getById(UUID id) {
        return experimentRepository.findById(id);
    }

    public Experiment create(Experiment experiment) {
        Experiment newExperiment = experimentTool.create(
            experiment.getId(), //todo what id id is null?
            experiment.getName(),
            experiment.getPhoneBrand(),
            experiment.getPhoneModel(),
            experiment.getPhoneSerial(),
            experiment.getExTimestamp(),
            experiment.getComment()
        );
        return experimentRepository.save(newExperiment);
    }

    public Optional<Experiment> update(UUID id, Experiment experiment) {
        //todo check if id == experiment.getId
        return experimentRepository.findById(id)
                .map(experimentOld -> experimentTool.update(experimentOld, experiment))
                .map(experimentRepository::save);
    }

    public void delete(UUID id) {
        experimentRepository.deleteById(id);
    }

    public Experiment createWithData(Experiment experiment, List<ExperimentData> experimentDataList){
        Experiment newExperiment = experimentTool.create(
                experiment.getId(), //todo what id id is null?
                experiment.getName(),
                experiment.getPhoneBrand(),
                experiment.getPhoneModel(),
                experiment.getPhoneSerial(),
                experiment.getExTimestamp(),
                experiment.getComment()
        );
        var saved = experimentRepository.save(newExperiment);
        experimentDataList.forEach(experimentDataService::create);//todo saveall
        return saved;
    }
}

