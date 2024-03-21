package com.anorisno.appDataTool.controller.dto;


import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class ExperimentWithDataDTO {

    private String name;
    private String phoneBrand;
    private String phoneModel;
    private String phoneSerial;
    private ZonedDateTime exTimestamp;
    private List<DataDTO> values;
}
