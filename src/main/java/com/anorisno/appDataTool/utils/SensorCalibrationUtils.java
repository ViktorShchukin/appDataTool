package com.anorisno.appDataTool.utils;


import com.anorisno.appDataTool.model.ExperimentData;
import com.anorisno.appDataTool.model.SensorData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SensorCalibrationUtils {

    /**
     * кол во семплов для определения bias = 150
     */
    public int calculateRequiredSamples(
            List<ExperimentData> data,
            String axis,
            double tolerance) {

        if (tolerance <= 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }

        double standardDeviation = calculateStandardDeviation(data, axis);

        return Math.max(2, (int) Math.ceil(
                Math.pow(1.96 * standardDeviation / tolerance, 2)
        ));
    }


    static public double calculateStandardDeviation(List<ExperimentData> data, String axis) {
        double mean = data.stream()
                .mapToDouble(d -> getAxisValue(d, axis))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("No sensor data"));

        if (data.size() < 2) {
            throw new IllegalArgumentException("At least 2 samples are required");
        }

        double sumSquaredDifferences = data.stream()
                .mapToDouble(d -> Math.pow(getAxisValue(d, axis) - mean, 2))
                .sum();

        return Math.sqrt(sumSquaredDifferences / (data.size() - 1));
    }



    static public double getAxisValue(ExperimentData data, String axis) {
        return switch (axis.toLowerCase()) {
            case "x" -> data.getAccx();
            case "y" -> data.getAccy();
            case "z" -> data.getAccz();
            default -> throw new IllegalArgumentException("Axis must be x, y, or z");
        };
    }


    /**
     * Parses a CSV file with format: sensortype;timestamp;x;y;z
     *
     * @param csvFile File object pointing to the .csv file
     * @return List of parsed SensorData records
     * @throws IOException if reading the file fails
     */
    public static List<SensorData> parseCsvFile(File csvFile) throws IOException {
        List<SensorData> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Skip CSV Header
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.toLowerCase().startsWith("sensortype")) {
                        continue;
                    }
                }

                String[] parts = line.split(";");
                if (parts.length < 5) {
                    continue; // Skip malformed lines
                }

                try {
                    // Column 0: sensortype (TYPE_LINEAR_ACCELERATION or TYPE_ROTATION_VECTOR)
                    SensorData.SensorType type = SensorData.SensorType.valueOf(parts[0].trim());

                    // Column 1: timestamp
                    Double timestamp = Double.parseDouble(parts[1].trim());

                    // Column 2, 3, 4: x, y, z
                    Double x = Double.parseDouble(parts[2].trim());
                    Double y = Double.parseDouble(parts[3].trim());
                    Double z = Double.parseDouble(parts[4].trim());

                    // SensorData record constructor: (type, x, y, z, timestamp)
                    records.add(new SensorData(type, x, y, z, timestamp));

                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping malformed row: " + line + " (" + e.getMessage() + ")");
                }
            }
        }

        return records;
    }

    /** Helper function to convert a rotation vector to a rotation matrix.
     *  Given a rotation vector (presumably from a ROTATION_VECTOR sensor), returns a
     *  9  or 16 element rotation matrix in the array R.  R must have length 9 or 16.
     *  If R.length == 9, the following matrix is returned:
     * <pre>
     *   /  R[ 0]   R[ 1]   R[ 2]   \
     *   |  R[ 3]   R[ 4]   R[ 5]   |
     *   \  R[ 6]   R[ 7]   R[ 8]   /
     *</pre>
     * If R.length == 16, the following matrix is returned:
     * <pre>
     *   /  R[ 0]   R[ 1]   R[ 2]   0  \
     *   |  R[ 4]   R[ 5]   R[ 6]   0  |
     *   |  R[ 8]   R[ 9]   R[10]   0  |
     *   \  0       0       0       1  /
     *</pre>
     *  @param rotationVector the rotation vector to convert
     */
    public static double[] getRotationMatrixFromVector(double[] rotationVector) {

        double[] R = new double[]{
                0,0,0,
                0,0,0,
                0,0,0
        };

        double q0;
        double q1 = rotationVector[0];
        double q2 = rotationVector[1];
        double q3 = rotationVector[2];

        if (rotationVector.length >= 4) {
            q0 = rotationVector[3];
        } else {
            q0 = 1 - q1 * q1 - q2 * q2 - q3 * q3;
            q0 = (q0 > 0) ? (double) Math.sqrt(q0) : 0;
        }

        double sq_q1 = 2 * q1 * q1;
        double sq_q2 = 2 * q2 * q2;
        double sq_q3 = 2 * q3 * q3;
        double q1_q2 = 2 * q1 * q2;
        double q3_q0 = 2 * q3 * q0;
        double q1_q3 = 2 * q1 * q3;
        double q2_q0 = 2 * q2 * q0;
        double q2_q3 = 2 * q2 * q3;
        double q1_q0 = 2 * q1 * q0;

        if (R.length == 9) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;

            R[3] = q1_q2 + q3_q0;
            R[4] = 1 - sq_q1 - sq_q3;
            R[5] = q2_q3 - q1_q0;

            R[6] = q1_q3 - q2_q0;
            R[7] = q2_q3 + q1_q0;
            R[8] = 1 - sq_q1 - sq_q2;
        } else if (R.length == 16) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;
            R[3] = 0.0f;

            R[4] = q1_q2 + q3_q0;
            R[5] = 1 - sq_q1 - sq_q3;
            R[6] = q2_q3 - q1_q0;
            R[7] = 0.0f;

            R[8] = q1_q3 - q2_q0;
            R[9] = q2_q3 + q1_q0;
            R[10] = 1 - sq_q1 - sq_q2;
            R[11] = 0.0f;

            R[12] = R[13] = R[14] = 0.0f;
            R[15] = 1.0f;
        }
        return R;
    }

    /**
     * @param data array of samples
     * @return histogram with 0.001(bins) precision
     */
    public static Map<Double, Integer> getHistogram(List<Double> data) {
        var map = new TreeMap<Double, Integer>();
        for (Double item : data) {
            double value = Math.round(item * 1000) / 1000.0;
            if (map.containsKey(value)) {
                map.put(value, map.get(value) + 1);
            } else {
                map.put(value, 1);
            }
        }
        return map;
    }
}
