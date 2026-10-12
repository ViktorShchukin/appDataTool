package com.anorisno.appDataTool;

import com.anorisno.appDataTool.model.ExperimentData;
import com.anorisno.appDataTool.service.ExperimentDataService;
import com.anorisno.appDataTool.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@SpringBootApplication
//@EnableJpaRepositories
@RequiredArgsConstructor
public class PrintSensorData implements CommandLineRunner {

    private final ExperimentService experimentService;

    public static void main(String[] args) {
        SpringApplication app =
                new SpringApplication(AppDataToolApplication.class);

        app.setAdditionalProfiles("cli");
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        var chart = new XYChart(1000, 500);
        chart.setTitle("Sensor Data");
        chart.setXAxisTitle("timestamp");
        chart.setYAxisTitle("value");

        List<ExperimentData> data = experimentService.getByExperimentName("rest");
        chart.addSeries(
                "acc x",
                data.stream().map(ExperimentData::getValueTimestamp).toList(),
                data.stream().map(ExperimentData::getAccx).toList()
        );

        BitmapEncoder.saveBitmap(
                chart,
//                1,
//                1,
                "target/chart.png",
                BitmapEncoder.BitmapFormat.PNG
        );
    }

}
