package com.anorisno.appDataTool.repository;

import com.anorisno.appDataTool.model.ExperimentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExperimentDataRepository extends JpaRepository<ExperimentData, UUID> {


    List<ExperimentData> findAllByExperimentId(UUID experimentId);


    //example of handmade query with HQL
    @Query(value = "from ExperimentData e where e.experimentId = :experimentId") //HQL
    List<ExperimentData> findByExId(@Param("experimentId") UUID experimentId);

    //example of handmade query with raw SQL
    @Query(value = "select * from experiment_data e where e.experiment_id = :experimentId", nativeQuery = true)
    List<ExperimentData> findByExIdNative(@Param("experimentId") UUID experimentId);
}
