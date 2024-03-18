package com.anorisno.appDataTool.controller.mappers;


import com.anorisno.appDataTool.confiig.AppMapperConfig;
import com.anorisno.appDataTool.controller.dto.ExperimentDTO;
import com.anorisno.appDataTool.model.Experiment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = AppMapperConfig.class)
public interface ExperimentMapper {

//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "name", source = "name")
//    @Mapping(target = "phoneBrand", source = "phoneBrand")
//    @Mapping(target = "phoneModel", source = "phoneModel")
//    @Mapping(target = "phoneSerial", source = "phoneSerial")
//    @Mapping(target = "exTimestamp", source = "exTimestamp")
//    @Mapping(target = "comment", source = "comment")
    ExperimentDTO mapToDto(Experiment experiment);

    Experiment mapFromDto(ExperimentDTO dto);

    List<ExperimentDTO> mapToDto(List<Experiment> dtoList);
}
