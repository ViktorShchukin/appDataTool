package com.anorisno.appDataTool.controller.mappers;

import com.anorisno.appDataTool.confiig.AppMapperConfig;
import com.anorisno.appDataTool.controller.dto.ExperimentDataDTO;
import com.anorisno.appDataTool.model.ExperimentData;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = AppMapperConfig.class)
public interface ExperimentDataMapper {
    List<ExperimentDataDTO> mapToDTO(List<ExperimentData> experimentDataList);

    ExperimentDataDTO mapToDTO(ExperimentData experimentData);

    ExperimentData fromDTO(ExperimentDataDTO experimentDataDTO);
}
