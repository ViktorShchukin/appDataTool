package com.anorisno.appDataTool.controller.dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.UUID;

@Data
public class ExperimentDataDTO {

    private UUID id;
    private UUID experimentId;
    private double accx;
    private double accy;
    private double accz;
    private long valueTimestamp;
}
