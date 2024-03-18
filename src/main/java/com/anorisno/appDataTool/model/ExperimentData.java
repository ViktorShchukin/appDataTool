package com.anorisno.appDataTool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "experiment_data")
public class ExperimentData {

    @Id
    private UUID id;

    @Column(name = "experiment_id")
    private UUID experimentId;

    @Column(name = "acc_x")
    private double accx;

    @Column(name = "acc_y")
    private double accy;

    @Column(name = "acc_z")
    private double accz;

    @Column(name = "value_timestamp")
    private long valueTimestamp;


}
