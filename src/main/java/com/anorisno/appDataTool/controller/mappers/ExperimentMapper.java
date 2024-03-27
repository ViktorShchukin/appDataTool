package com.anorisno.appDataTool.controller.mappers;


import com.anorisno.appDataTool.confiig.AppMapperConfig;
import com.anorisno.appDataTool.controller.dto.DataDTO;
import com.anorisno.appDataTool.controller.dto.ExperimentDTO;
import com.anorisno.appDataTool.controller.dto.ExperimentWithDataDTO;
import com.anorisno.appDataTool.model.Experiment;
import com.anorisno.appDataTool.model.ExperimentData;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(config = AppMapperConfig.class)
public interface ExperimentMapper {

//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "name", source = "name")
//    @Mapping(target = "phoneBrand", source = "phoneBrand")
//    @Mapping(target = "phoneModel", source = "phoneModel")
//    @Mapping(target = "phoneSerial", source = "phoneSerial")
//    @Mapping(target = "exTimestamp", source = "exTimestamp")
//    @Mapping(target = "comment", source = "comment")
    ExperimentDTO mapToDTO(Experiment experiment);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    Experiment mapFromDTO(ExperimentDTO dto);

    List<ExperimentDTO> mapToDTO(List<Experiment> dtoList);

//    ExperimentWithDataDTO mapToExperimentWithDataDTO ();

//    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
//    @Mapping(target = "comment", ignore = true)
//    @Mapping(target = "name", source = "name")
//    @Mapping(target = "phoneBrand", source = "phoneBrand")
//    @Mapping(target = "phoneModel", source = "phoneModel")
//    @Mapping(target = "phoneSerial", source = "phoneSerial")
//    @Mapping(target = "exTimestamp", source = "exTimestamp")
//    Experiment getExperimentFromExperimentWithDataDTO(ExperimentWithDataDTO experimentWithDataDTO);

    List<ExperimentData> getAllExperimentDataFromExperimentWithDataDTO(List<DataDTO> experimentWithDataDTO, @Context UUID expId);


    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "experimentId", expression = "java(expId)")
    @Mapping(target = "accx", source = "dataDTO.accx")
    @Mapping(target = "accy", source = "dataDTO.accy")
    @Mapping(target = "accz", source = "dataDTO.accz")
    @Mapping(target = "valueTimestamp", source = "dataDTO.valueTimestamp")
    ExperimentData experimentDataFromDataDto(DataDTO dataDTO, @Context UUID expId);

//    @AfterMapping()
//    default void fillExperimentData(@MappingTarget ExperimentData experimentData, UUID expId){
//        experimentData.setExperimentId(expId);
//    }
}
