package com.anorisno.appDataTool.controller.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

public class ExperimentDTO {

    private UUID id; //todo in database it has uuid type. need to be matched
    private String name;
    private String phoneBrand;
    private String phoneModel;
    private String phoneSerial;
    private Instant exTimestamp; //todo instant or zoned datetime???
    private String comment;
}
