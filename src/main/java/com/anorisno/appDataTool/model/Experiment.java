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
@Table(name = "experiment")
public class Experiment {

    @Id
    private UUID id; //todo in database it has uuid type. need to be matched

    @Column(name = "name") //todo add colunm to all properties
    private String name;

    @Column(name = "phone_brand")
    private String phoneBrand;

    @Column(name = "phone_model")
    private String phoneModel;

    @Column(name = "phone_serial")
    private String phoneSerial;

    @Column(name = "ex_timestamp")
    private ZonedDateTime exTimestamp; //todo instant or zoned datetime???

    @Column(name = "comment")
    private String comment;
}
