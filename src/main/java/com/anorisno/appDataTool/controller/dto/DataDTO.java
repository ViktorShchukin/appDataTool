package com.anorisno.appDataTool.controller.dto;

import lombok.Data;

@Data
public class DataDTO {

    private String sensorType;
    private double accx;
    private double accy;
    private double accz;
    private long valueTimestamp;
}
