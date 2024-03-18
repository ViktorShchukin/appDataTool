package com.anorisno.appDataTool.service.tools;

import com.anorisno.appDataTool.confiig.AppMapperConfig;
import com.anorisno.appDataTool.model.ExperimentData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(config = AppMapperConfig.class)
public interface ExperimentDataTool {

    ExperimentData create(UUID id,
                          UUID experimentId,
                          double accx,
                          double accy,
                          double accz,
                          long valueTimestamp
    );

    @Mapping(target = "id", ignore = true)
    ExperimentData update(@MappingTarget ExperimentData toUpdate, ExperimentData fromUpdate);
}
