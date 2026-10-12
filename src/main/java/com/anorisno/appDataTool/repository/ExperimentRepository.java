package com.anorisno.appDataTool.repository;


import com.anorisno.appDataTool.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperimentRepository extends JpaRepository<Experiment, UUID> {
    Optional<Experiment> getByName(String experimentName);
}
