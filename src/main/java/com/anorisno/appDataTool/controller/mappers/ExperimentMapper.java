package com.anorisno.appDataTool.controller.mappers;


import com.anorisno.appDataTool.confiig.AppMapperConfig;
import com.anorisno.appDataTool.controller.dto.ExperimentDTO;
import com.anorisno.appDataTool.model.Experiment;
import org.mapstruct.Mapper;

@Mapper(config = AppMapperConfig.class)
public interface ExperimentMapper {
    ExperimentDTO mapToDto(Experiment experiment);
}
