package com.anorisno.appDataTool;

import com.anorisno.appDataTool.model.SensorData;
import com.anorisno.appDataTool.utils.SensorCalibrationUtils;

import java.util.List;
import java.util.Map;

// TODO: unit test for all of this.
public class Ins {

    private final Zupt zupt = new Zupt();

    private InsState state;

    public void onAcceleration(SensorData sensorData, List<SensorData> calibratingData) {
        if (state == null) {
            init(sensorData.timestamp());
        }

        // skip unordered
        if (state.timestamp() > sensorData.timestamp()) {
            return;
        }

        double[] filtered = filterAcc(sensorData.data(), calibratingData);
        double[] acc = rotateVector(filtered);
        // TODO: check if working.
        boolean checkRes = zupt.check(acc, sensorData.timestamp());
        System.out.println("checkRes: " + checkRes);
        if (checkRes) {
            state = state.stasis(sensorData.timestamp());
            return;
        }

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
        if (state.timestamp() > rotation.timestamp()) {
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

    private double[] accBias;

    private double[] filterAcc(double[] acc, List<SensorData> calibratingData) {
        if (accBias == null) {
            double[] mean = new double[3];
            int n = 0;

            for (SensorData sample : calibratingData) {
                if (sample.type() != SensorData.SensorType.TYPE_LINEAR_ACCELERATION) {
                    continue;
                }

                double[] value = sample.data();

                n++;
                for (int i = 0; i < 3; i++) {
                    mean[i] += (value[i] - mean[i]) / n;
                }
                var some = 10;
            }

            if (n < 2) {
                throw new IllegalArgumentException("Missing stationary calibration");
            }
            accBias = mean;
        }

        var acc2 = new double[]{
                acc[0] - accBias[0],
                acc[1] - accBias[1],
                acc[2] - accBias[2]
        };
        var accNorm = SensorCalibrationUtils.getNorm(acc2);
        if (accNorm < 0.05) {
            return new double[]{0, 0, 0};
        }

        return acc2;
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

    private void init(long timestamp) {
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
            long timestamp
    ) {

        public InsState withOrientation(double[] n_orientation) {
            return new InsState(n_orientation, acceleration, velocity, position, timestamp);
        }

        public InsState stasis(long timestamp) {
            return new InsState(orientation, new double[]{0, 0, 0}, new double[]{0, 0, 0}, position, timestamp);
        }

        public InsState update(
                double[] n_acceleration,
                double[] n_velocity,
                double[] n_position,
                long n_timestamp
        ) {
            return new InsState(orientation, n_acceleration, n_velocity, n_position, n_timestamp);
        }
    }

    public InsState getState() {
        return state;
    }


}
