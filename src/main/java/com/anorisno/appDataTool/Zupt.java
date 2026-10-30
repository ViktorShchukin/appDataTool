package com.anorisno.appDataTool;

import com.anorisno.appDataTool.utils.SensorCalibrationUtils;

/**
 * ZUPT stands for Zero Update Position and Timing.
 * find moments where ins is not moving.
 */
public class Zupt {
    // 100 ms
    private static final long RESET_GAP = 200_000_000;

    private final long STASIS_THRESHOLD = 400_000_000;

    private long lastTimestamp = -1;
    private long lastMotionTimestamp = -1;

    /**
     *
     * @param acc       acceleration
     * @param timestamp in nanos. expects ordered
     * @return true if not moving
     */
    public boolean check(double[] acc, long timestamp) {
        if (lastTimestamp < 0) {
            lastTimestamp = timestamp;
            lastMotionTimestamp = timestamp;
            return false;
        }
        // reset if measurements are lost
        if (timestamp - lastTimestamp > RESET_GAP) {
            lastTimestamp = timestamp;
            lastMotionTimestamp = timestamp;
            return false;
        }

        var norm = SensorCalibrationUtils.getNorm(acc);
        if (norm > 0.05) {
            lastMotionTimestamp = timestamp;
        }

        lastTimestamp = timestamp;

        return lastMotionTimestamp >= 0
                && timestamp - lastMotionTimestamp > STASIS_THRESHOLD;
    }
}
