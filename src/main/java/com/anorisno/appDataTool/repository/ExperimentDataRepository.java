package com.anorisno.appDataTool.repository;

import com.anorisno.appDataTool.model.ExperimentData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExperimentDataRepository extends JpaRepository<ExperimentData, UUID> {
}
