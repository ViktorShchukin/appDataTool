package com.anorisno.appDataTool;

import com.anorisno.appDataTool.model.SensorData;
import com.anorisno.appDataTool.utils.SensorCalibrationUtils;

import java.util.List;
import java.util.Map;

// TODO: unit test for all of this.
public class Ins {

    private InsState state;

    public void onAcceleration(SensorData sensorData, List<SensorData> calibratingData) {
        if (state == null) {
            init(sensorData.timestamp());
        }

        // skip unordered
        if(state.timestamp() > sensorData.timestamp()) {
            return;
        }

        double[] filtered = filterAcc(sensorData.data(), calibratingData);
        double[] acc = rotateVector(filtered);
        // TODO: check if working.
//        if(zupt(acc, sensorData.timestamp())){
//
//        }

        double dt = (sensorData.timestamp() - state.timestamp()) / 1e9;
        double[] newVelocity = getVelocity(acc, state, dt);
        double[] newPosition = getPosition(newVelocity, state, dt);

        var some = 5;

        var newState = state.update(
                acc,
                newVelocity,
                newPosition,
                sensorData.timestamp()
        );

//        newState = checkZupt(newState);

        state = newState;

        var some2 = 10;
    }

    public void updateOrientation(SensorData rotation) {
        if (state == null) {
            return;
        }
        // skip unordered
        if(state.timestamp() > rotation.timestamp()) {
            return;
        }
        state = state.withOrientation(rotation.data());
    }


    private double[] rotateVector(double[] v) {
        var R = SensorCalibrationUtils.getRotationMatrixFromVector(state.orientation);
        var res = new double[]{
                R[0] * v[0] + R[1] * v[1] + R[2] * v[2],
                R[3] * v[0] + R[4] * v[1] + R[5] * v[2],
                R[6] * v[0] + R[7] * v[1] + R[8] * v[2]
        };
        return res;
    }

    private static double[] integrate(
            double dx,
            double[] f1,
            double[] f2
    ) {
        double[] res = new double[f1.length];
        for (int i = f1.length - 1; i >= 0; i--) {
            res[i] = (f1[i] + f2[i]) / 2 * dx;
        }
        return res;
    }


    private double[] filterAcc(double[] acc, List<SensorData> calibratingData) {
        var data = calibratingData.stream().filter(item -> item.type().equals(SensorData.SensorType.TYPE_LINEAR_ACCELERATION)).map(SensorData::data).toList();
        Map<Double, Integer> hist_x = SensorCalibrationUtils.getHistogram(data.stream().map(item -> item[0]).toList());
        var hist_y = SensorCalibrationUtils.getHistogram(data.stream().map(item -> item[1]).toList());
        var hist_z = SensorCalibrationUtils.getHistogram(data.stream().map(item -> item[2]).toList());
        var max_x = hist_x.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        var max_y = hist_y.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        var max_z = hist_z.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        var accNorm = Math.pow(acc[0], 2) + Math.pow(acc[1], 2) + Math.pow(acc[2], 2);
        var calibrationNorm = Math.pow(max_x, 2) + Math.pow(max_y, 2) + Math.pow(max_z, 2);
        if (accNorm < 0.1) {
            return new double[]{0, 0, 0};
        }

        return acc;
    }

    private static double[] getVelocity(double[] acc, InsState state, double dt) {

        var deltaVelocity = integrate(
                dt,
                state.acceleration,
                acc
        );
        return new double[]{
                state.velocity[0] + deltaVelocity[0],
                state.velocity[1] + deltaVelocity[1],
                state.velocity[2] + deltaVelocity[2]
        };
    }

    private static double[] getPosition(double[] velocity, InsState state, double dt) {
        var deltaPosition = integrate(
                dt,
                state.velocity,
                velocity
        );
        return new double[]{
                state.position[0] + deltaPosition[0],
                state.position[1] + deltaPosition[1],
                state.position[2] + deltaPosition[2]
        };
    }

    private void init(double timestamp) {
        state = new InsState(
                new double[]{0, 0, 0},
                new double[]{0, 0, 0},
                new double[]{0, 0, 0},
                new double[]{0, 0, 0},
                timestamp
        );
    }

    public record InsState(
            double[] orientation,
            double[] acceleration,
            double[] velocity,
            double[] position,
            double timestamp
    ) {

        public InsState withOrientation(double[] n_orientation) {
            return new InsState(n_orientation, acceleration, velocity, position, timestamp);
        }

        public InsState update(
                double[] n_acceleration,
                double[] n_velocity,
                double[] n_position,
                double n_timestamp
        ) {
            return new InsState(orientation, n_acceleration, n_velocity, n_position, n_timestamp);
        }
    }

    InsState getState() {
        return state;
    }

    private long lastTimestamp = -1;
    private long lastMotionTimestamp = -1;

    boolean zupt(double[] acc, long timestamp) {
        if (lastTimestamp < 0) {
            lastTimestamp = timestamp;
            return false;
        }
        // check for if gap between measurements bigger 200 ms.
        if (lastTimestamp - timestamp > 200_000_000) {
            lastMotionTimestamp = timestamp;
            return false;
        }

        if (getNorm(acc) > 0.1) {
            lastMotionTimestamp = timestamp;
        }

        return lastMotionTimestamp > 0
                && timestamp - lastMotionTimestamp > 400_000_000;
    }

    double getNorm(double[] vector) {
        var accumulator = 0.0;
        for (int i = 0; i < vector.length; i++) {
            var v = vector[i] * vector[i];
            accumulator += v;
        }
        return Math.sqrt(accumulator);
    }
}
