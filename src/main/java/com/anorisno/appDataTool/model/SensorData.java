package com.anorisno.appDataTool.model;

public record SensorData(
        SensorType type,
        Double x,
        Double y,
        Double z,
        Double timestamp
) {

    public double[] data() {
        return new double[]{x, y, z};
    }

    public static enum SensorType {
        TYPE_LINEAR_ACCELERATION,
        TYPE_ROTATION_VECTOR
    }
}
