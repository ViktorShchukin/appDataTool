package com.anorisno.appDataTool.service.tools;

import com.anorisno.appDataTool.confiig.AppMapperConfig;
import com.anorisno.appDataTool.model.Experiment;
import jakarta.persistence.Column;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;
import java.time.ZonedDateTime;
import java.util.UUID;

@Mapper(config = AppMapperConfig.class)
public interface ExperimentTool {

    @Mapping(target = "id", ignore = true)
    Experiment update(@MappingTarget Experiment toUpdate, Experiment fromUpdate);

    Experiment create(UUID id,
                      String name,
                      String phoneBrand,
                      String phoneModel,
                      String phoneSerial,
                      ZonedDateTime exTimestamp,
                      String comment);
}
