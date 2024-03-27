package com.anorisno.appDataTool.controller.dto;


import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class ExperimentWithDataDTO {

    private ExperimentDTO experiment;
    private List<DataDTO> dataList;
}
