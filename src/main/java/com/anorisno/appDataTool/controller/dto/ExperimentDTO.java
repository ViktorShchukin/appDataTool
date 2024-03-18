package com.anorisno.appDataTool.controller.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class ExperimentDTO {

    private UUID id;
    private String name;
    private String phoneBrand;
    private String phoneModel;
    private String phoneSerial;
    private ZonedDateTime exTimestamp;
    private String comment;
}
