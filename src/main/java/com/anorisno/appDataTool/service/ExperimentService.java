package com.anorisno.appDataTool.service;


import com.anorisno.appDataTool.model.Experiment;
import com.anorisno.appDataTool.repository.ExperimentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;

    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }


    @Transactional
    public List<Experiment> findAll() {return experimentRepository.findAll();
    }
}
