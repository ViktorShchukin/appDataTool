package com.anorisno.appDataTool;

import com.anorisno.appDataTool.model.ExperimentData;
import com.anorisno.appDataTool.model.SensorData;
import com.anorisno.appDataTool.service.ExperimentService;
import com.anorisno.appDataTool.utils.SensorCalibrationUtils;
import lombok.RequiredArgsConstructor;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.*;

//@SpringBootApplication
//@EnableJpaRepositories
//@RequiredArgsConstructor
public class PrintSensorData
//        implements CommandLineRunner
{

//    private final ExperimentService experimentService;

    public static void main(String[] args) throws Exception {

        String filename =
//                "261013real1_20261013_194600.csv"
//                "261013rest.csv"
//                "261016rest_20261016_091632.csv"
//                "261016rest2_20261016_092855.csv"
//                "261013forward1_20261013_193749.csv"
                "261013forward2_20261013_193900.csv"
//                "rest_20261019_090413.csv"
//                "rest-y-up_20261019_091114.csv"
//                "20261020_restacc.csv"
//                "20261020_rest_acc_uncolibrated.csv"
//                "20261020________1.csv"
                ;
        var file = Path.of("/home/anorisno/work/git-hub-projects/appDataTool/experiments/" + filename).toFile();
        var correctFilename =
//                "261016rest_20261016_091632.csv"
                "261013rest.csv"
                ;
        var correctFile = Path.of("/home/anorisno/work/git-hub-projects/appDataTool/experiments/" + correctFilename).toFile();
        var res = SensorCalibrationUtils.parseCsvFile(file);
        var calibrateData = SensorCalibrationUtils.parseCsvFile(correctFile);
        printIns(res, calibrateData);
        printRowData(
                res.stream()
                        .filter(item -> item.type().equals(SensorData.SensorType.TYPE_LINEAR_ACCELERATION))
                        .toList(),
                "row_acceleration");
        printRowData(
                res.stream()
                        .filter(item -> item.type().equals(SensorData.SensorType.TYPE_ROTATION_VECTOR))
                        .toList(),
                "row_gyroscope");
//        printHist(res, filename + "_hist");

    }

    public static void printIns(List<SensorData> list, List<SensorData> calibrateData) throws Exception {
        var ins = new Ins();
        List<Ins.InsState> stateList = new ArrayList<>();
        for (SensorData item : list) {
            switch (item.type()) {
                case TYPE_ROTATION_VECTOR -> ins.updateOrientation(item);
                case TYPE_LINEAR_ACCELERATION -> ins.onAcceleration(item, calibrateData);
            }
            if (ins.getState() == null) {
                continue;
            }
            stateList.add(ins.getState());
        }

        printChart(stateList);
    }

//    public static void main(String[] args) {
//        SpringApplication app =
//                new SpringApplication(AppDataToolApplication.class);
//
//        app.setAdditionalProfiles("cli");
//        app.run(args);
//    }

//    @Override
//    public void run(String... args) throws Exception {
//        List<ExperimentData> data = experimentService.getByExperimentName("rest");

//        var x = calculateBias(data, "x");
//        var y = calculateBias(data, "y");
//        var z = calculateBias(data, "z");
//
//        List<ExperimentData> data2 = experimentService.getByExperimentName("rest2");
//
//        var x2 = calculateBias(data2, "x");
//        var y2 = calculateBias(data2, "y");
//        var z2 = calculateBias(data2, "z");
//        System.out.printf("screen up:     {%f, %f, %f}\n", x, y, z);
//        System.out.printf("rignt edge up: {%f, %f, %f}\n", x2, y2, z2);
//    }


    static private void printChart(
            List<Ins.InsState> data
    ) throws Exception {
        var chart = new XYChart(1000, 500);
        var chart2 = new XYChart(1000, 500);
        var chart3 = new XYChart(1000, 500);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart2.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart3.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.setTitle("Sensor Data");
        chart.setXAxisTitle("timestamp");
        chart.setYAxisTitle("value");

        chart.addSeries(
                "position x",
                data.stream()
                        .map(Ins.InsState::timestamp)
                        .toList(),
                data.stream()
                        .map(item -> item.position()[0])
                        .toList()
        );

        chart2.addSeries(
                "position y",
                data.stream()
                        .map(Ins.InsState::timestamp)
                        .toList(),
                data.stream()
                        .map(item -> item.position()[1])
                        .toList()
        );

        chart3.addSeries(
                "position z",
                data.stream()
                        .map(Ins.InsState::timestamp)
                        .toList(),
                data.stream()
                        .map(item -> item.position()[2])
                        .toList()
        );
        BitmapEncoder.saveBitmap(
                List.of(chart, chart2, chart3),
                3,
                1,
                "target/chart.png",
                BitmapEncoder.BitmapFormat.PNG
        );
    }

    public static void printHist(List<SensorData> list, String outName) throws Exception {
        var data = list.stream().filter(item -> item.type().equals(SensorData.SensorType.TYPE_LINEAR_ACCELERATION)).map(SensorData::data).toList();
        var hist_x = SensorCalibrationUtils.getHistogram(data.stream().map(item -> item[0]).toList());
        var hist_y = SensorCalibrationUtils.getHistogram(data.stream().map(item -> item[1]).toList());
        var hist_z = SensorCalibrationUtils.getHistogram(data.stream().map(item -> item[2]).toList());

        var chart_x = new CategoryChart(1000, 500);
        var chart_y = new CategoryChart(1000, 500);
        var chart_z = new CategoryChart(1000, 500);

        chart_x.setTitle("x");
        chart_y.setTitle("y");
        chart_z.setTitle("z");

        chart_x.addSeries(
                "x",
                hist_x.keySet().stream().toList(),
                hist_x.values().stream().toList()
        );
        chart_y.addSeries(
                "y",
                hist_y.keySet().stream().toList(),
                hist_y.values().stream().toList()
        );
        chart_z.addSeries(
                "z",
                hist_z.keySet().stream().toList(),
                hist_z.values().stream().toList()
        );

        BitmapEncoder.saveBitmap(
                List.of(chart_x, chart_y, chart_z),
                3,
                1,
                "target/%s.png".formatted(outName),
                BitmapEncoder.BitmapFormat.PNG
        );
    }

    public double calculateBias(List<ExperimentData> data, String axis) {
        return data.stream()
                .mapToDouble(d -> SensorCalibrationUtils.getAxisValue(d, axis))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("No sensor data"));
    }


    public static void printRowData(List<SensorData> data, String outName) throws Exception {
        var chart = new XYChart(1000, 500);
        var chart2 = new XYChart(1000, 500);
        var chart3 = new XYChart(1000, 500);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart2.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart3.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.setTitle("Sensor Data");
        chart.setXAxisTitle("timestamp");
        chart.setYAxisTitle("value");

        chart.addSeries(
                "position x",
                data.stream()
                        .map(SensorData::timestamp)
                        .toList(),
                data.stream()
                        .map(item -> item.data()[0])
                        .toList()
        );

        chart2.addSeries(
                "position y",
                data.stream()
                        .map(SensorData::timestamp)
                        .toList(),
                data.stream()
                        .map(item -> item.data()[1])
                        .toList()
        );

        chart3.addSeries(
                "position z",
                data.stream()
                        .map(SensorData::timestamp)
                        .toList(),
                data.stream()
                        .map(item -> item.data()[2])
                        .toList()
        );
        BitmapEncoder.saveBitmap(
                List.of(chart, chart2, chart3),
                3,
                1,
                "target/%s.png".formatted(outName),
                BitmapEncoder.BitmapFormat.PNG
        );

    }


}
