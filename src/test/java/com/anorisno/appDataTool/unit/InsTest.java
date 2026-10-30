package com.anorisno.appDataTool.unit;

import com.anorisno.appDataTool.Ins;
import com.anorisno.appDataTool.model.SensorData;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static com.anorisno.appDataTool.model.SensorData.SensorType.TYPE_LINEAR_ACCELERATION;
import static com.anorisno.appDataTool.model.SensorData.SensorType.TYPE_ROTATION_VECTOR;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Jupiter 6.1.3 tests for the three attachments dated 2026-10-30.
 * Place at src/test/java/com/anorisno/appDataTool/InsTest.java.
 * No Mockito or Spring context is needed. All timestamps are nanoseconds.
 *
 * Valid-motion tests use analytic results and the real Zupt/calibration utilities.
 * KnownProblems contains active regression tests; some fail on the supplied code.
 * Those tests require preserving orientation/history, removing stationary bias,
 * preserving real motion, rejecting invalid sensor inputs with
 * IllegalArgumentException, and preserving the input nanosecond timestamp.
 * They are desired behavior, not assertions that the current code is correct.
 *
 * SensorData construction follows the attached parser:
 * new SensorData(type, x, y, z, timestamp), where timestamp is a Long.
 * The stand-in DTOs used for isolated validation are not part of this test file.
 */
class InsTest {
    private static final double EPS = 1e-10;
    private static final long MS = 1_000_000L;
    private static final long STEP = 10 * MS;
    private static final long START = 1_000_000_000L;
    private static final double[] ZERO = {0, 0, 0};
    private static final List<SensorData> ZERO_CALIBRATION = List.of(
            acceleration(0, 0, 0, 0), acceleration(MS, 0, 0, 0));

    private static SensorData acceleration(long timestamp, double x, double y, double z) {
        return new SensorData(TYPE_LINEAR_ACCELERATION, x, y, z, timestamp);
    }

    private static SensorData rotation(long timestamp, double x, double y, double z) {
        return new SensorData(TYPE_ROTATION_VECTOR, x, y, z, timestamp);
    }

    private static void feed(Ins ins, long timestamp, double x, double y, double z) {
        ins.onAcceleration(acceleration(timestamp, x, y, z), ZERO_CALIBRATION);
    }

    private static Ins constantAcceleration(double x, double y, double z) {
        Ins ins = new Ins();
        for (int i = 0; i <= 100; i++) feed(ins, START + i * STEP, x, y, z);
        return ins;
    }

    @Test
    void stateIsAbsentBeforeFirstMeasurement() {
        assertNull(new Ins().getState());
    }

    @Test
    void firstAccelerationSeedsHistoryWithoutInventingElapsedTime() {
        Ins ins = new Ins();
        feed(ins, START, 1, 0, 0);
        assertArrayEquals(new double[]{1, 0, 0}, ins.getState().acceleration(), EPS);
        assertArrayEquals(ZERO, ins.getState().velocity(), EPS);
        assertArrayEquals(ZERO, ins.getState().position(), EPS);
        assertEquals(START, ins.getState().timestamp(), 0.0);
    }

    @Test
    void stationaryPhoneStaysAtOriginAndUpdatesTime() {
        Ins ins = constantAcceleration(0, 0, 0);
        assertArrayEquals(ZERO, ins.getState().acceleration(), EPS);
        assertArrayEquals(ZERO, ins.getState().velocity(), EPS);
        assertArrayEquals(ZERO, ins.getState().position(), EPS);
        assertEquals(START + 100 * STEP, ins.getState().timestamp(), 0.0);
    }

    @ParameterizedTest(name = "constant acceleration [{0}, {1}, {2}] for one second")
    @MethodSource("constantAccelerationCases")
    void integratesConstantAccelerationInMetersAndSeconds(double x, double y, double z) {
        Ins ins = constantAcceleration(x, y, z);
        // v = a*t; p = a*t^2/2; here t = 1 second and initial v/p are zero.
        assertArrayEquals(new double[]{x, y, z}, ins.getState().velocity(), EPS);
        assertArrayEquals(new double[]{x / 2, y / 2, z / 2}, ins.getState().position(), EPS);
    }

    static Stream<Arguments> constantAccelerationCases() {
        return Stream.of(
                Arguments.of(1.0, 0.0, 0.0),
                Arguments.of(-1.0, 0.0, 0.0),
                Arguments.of(0.0, 2.0, 0.0),
                Arguments.of(0.0, 0.0, -1.0),
                Arguments.of(1.0, -2.0, 3.0));
    }

    @Test
    void usesActualNonuniformSampleIntervals() {
        Ins ins = new Ins();
        for (long millis : new long[]{0, 11, 37, 62, 85, 100}) {
            feed(ins, START + millis * MS, 2, 0, 0);
        }
        assertArrayEquals(new double[]{0.2, 0, 0}, ins.getState().velocity(), EPS);
        assertArrayEquals(new double[]{0.01, 0, 0}, ins.getState().position(), EPS);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rotatedMotionCases")
    void rotatesDeviceAccelerationBeforeIntegrating(String name, double[] rotation,
                                                    double[] device, double[] world) {
        Ins ins = new Ins();
        feed(ins, START, 0, 0, 0);
        ins.updateOrientation(rotation(START, rotation[0], rotation[1], rotation[2]));
        feed(ins, START + STEP, device[0], device[1], device[2]);
        assertArrayEquals(world, ins.getState().acceleration(), EPS);
        // Linear ramp from zero to world acceleration over the first 0.01 s.
        assertArrayEquals(new double[]{world[0] * 0.005, world[1] * 0.005, world[2] * 0.005},
                ins.getState().velocity(), EPS);
    }

    static Stream<Arguments> rotatedMotionCases() {
        double h = Math.sqrt(0.5);
        return Stream.of(
                Arguments.of("90 degrees X: body Y becomes world Z",
                        new double[]{h, 0, 0}, new double[]{0, 1, 0}, new double[]{0, 0, 1}),
                Arguments.of("90 degrees Y: body Z becomes world X",
                        new double[]{0, h, 0}, new double[]{0, 0, 1}, new double[]{1, 0, 0}),
                Arguments.of("90 degrees Z: body X becomes world Y",
                        new double[]{0, 0, h}, new double[]{1, 0, 0}, new double[]{0, 1, 0}),
                Arguments.of("180 degrees Z: body X becomes negative world X",
                        new double[]{0, 0, 1}, new double[]{1, 0, 0}, new double[]{-1, 0, 0}));
    }

    @Test
    void orientationEventDoesNotAdvanceAccelerationIntegrationTime() {
        Ins ins = new Ins();
        feed(ins, START, 1, 0, 0);
        ins.updateOrientation(rotation(START + 5 * MS, 0, 0, 0));
        assertEquals(START, ins.getState().timestamp(), 0.0);
        feed(ins, START + STEP, 1, 0, 0);
        assertArrayEquals(new double[]{0.01, 0, 0}, ins.getState().velocity(), EPS);
        assertArrayEquals(new double[]{0.00005, 0, 0}, ins.getState().position(), EPS);
    }

    @Test
    void olderAccelerationIsIgnoredWithoutPoisoningTheNextStep() {
        Ins actual = new Ins();
        Ins control = new Ins();
        for (Ins ins : List.of(actual, control)) {
            feed(ins, START, 1, 0, 0);
            feed(ins, START + 2 * STEP, 1, 0, 0);
        }
        feed(actual, START + STEP, 100, 100, 100);
        for (Ins ins : List.of(actual, control)) feed(ins, START + 3 * STEP, 1, 0, 0);
        assertArrayEquals(control.getState().velocity(), actual.getState().velocity(), EPS);
        assertArrayEquals(control.getState().position(), actual.getState().position(), EPS);
        assertArrayEquals(control.getState().acceleration(), actual.getState().acceleration(), EPS);
    }

    @Test
    void orientationOlderThanLatestAccelerationIsIgnored() {
        Ins ins = new Ins();
        feed(ins, START, 1, 0, 0);
        feed(ins, START + 2 * STEP, 1, 0, 0);
        ins.updateOrientation(rotation(START + STEP, 0, 0, Math.sqrt(0.5)));
        feed(ins, START + 3 * STEP, 1, 0, 0);
        assertArrayEquals(new double[]{1, 0, 0}, ins.getState().acceleration(), EPS);
    }

    @Test
    void accelerationAfterStasisUsesTheMostRecentTimestamp() {
        Ins ins = constantAcceleration(0, 0, 0); // One second, including ZUPT updates.
        feed(ins, START + 101 * STEP, 2, 0, 0);
        assertArrayEquals(new double[]{0.01, 0, 0}, ins.getState().velocity(), EPS);
        assertArrayEquals(new double[]{0.00005, 0, 0}, ins.getState().position(), EPS);
    }

    private static Ins.InsState exampleState() {
        return new Ins.InsState(new double[]{0.1, 0.2, 0.3}, new double[]{1, 2, 3},
                new double[]{4, 5, 6}, new double[]{7, 8, 9}, START);
    }

    @Test
    void stateWithOrientationPreservesKinematicsAndTime() {
        Ins.InsState before = exampleState();
        Ins.InsState after = before.withOrientation(new double[]{0, 0, 0.5});
        assertArrayEquals(new double[]{0, 0, 0.5}, after.orientation(), EPS);
        assertArrayEquals(before.acceleration(), after.acceleration(), EPS);
        assertArrayEquals(before.velocity(), after.velocity(), EPS);
        assertArrayEquals(before.position(), after.position(), EPS);
        assertEquals(before.timestamp(), after.timestamp(), 0.0);
        assertArrayEquals(new double[]{0.1, 0.2, 0.3}, before.orientation(), EPS);
    }

    @Test
    void stateUpdatePreservesOrientationAndUsesNewKinematics() {
        Ins.InsState before = exampleState();
        Ins.InsState after = before.update(new double[]{2, 3, 4}, new double[]{5, 6, 7},
                new double[]{8, 9, 10}, START + STEP);
        assertArrayEquals(before.orientation(), after.orientation(), EPS);
        assertArrayEquals(new double[]{2, 3, 4}, after.acceleration(), EPS);
        assertArrayEquals(new double[]{5, 6, 7}, after.velocity(), EPS);
        assertArrayEquals(new double[]{8, 9, 10}, after.position(), EPS);
        assertEquals(START + STEP, after.timestamp(), 0.0);
        assertArrayEquals(new double[]{4, 5, 6}, before.velocity(), EPS);
    }

    @Test
    void stateStasisClearsAccelerationAndVelocityAndHoldsPosition() {
        Ins.InsState before = exampleState();
        Ins.InsState after = before.stasis(START + STEP);
        assertArrayEquals(before.orientation(), after.orientation(), EPS);
        assertArrayEquals(ZERO, after.acceleration(), EPS);
        assertArrayEquals(ZERO, after.velocity(), EPS);
        assertArrayEquals(before.position(), after.position(), EPS);
        assertEquals(START + STEP, after.timestamp(), 0.0);
        assertArrayEquals(new double[]{4, 5, 6}, before.velocity(), EPS);
    }

    @Nested
    class KnownProblems {
        @Test
        void orientationReceivedBeforeFirstAccelerationIsUsed() {
            Ins ins = new Ins();
            ins.updateOrientation(rotation(START, 0, 0, Math.sqrt(0.5)));
            feed(ins, START, 1, 0, 0);
            assertArrayEquals(new double[]{0, 1, 0}, ins.getState().acceleration(), EPS,
                    "Remember the initial orientation instead of silently using identity");
        }

        @Test
        void outOfOrderRotationIsComparedWithLatestRotationTimestamp() {
            Ins ins = new Ins();
            feed(ins, START, 0, 0, 0);
            ins.updateOrientation(rotation(START + 20 * MS, 0, 0, Math.sqrt(0.5)));
            ins.updateOrientation(rotation(START + 10 * MS, 0, 0, 0));
            feed(ins, START + 30 * MS, 1, 0, 0);
            assertArrayEquals(new double[]{0, 1, 0}, ins.getState().acceleration(), EPS,
                    "An older rotation must not replace the newer orientation");
        }

        @Test
        void stationaryCalibrationRemovesPerAxisBias() {
            Ins ins = new Ins();
            List<SensorData> calibration = List.of(
                    acceleration(0, 0.5, -0.25, 0.125),
                    acceleration(MS, 0.5, -0.25, 0.125));
            for (int i = 0; i <= 100; i++) {
                ins.onAcceleration(acceleration(START + i * STEP, 0.5, -0.25, 0.125), calibration);
            }
            assertAll(
                    () -> assertArrayEquals(ZERO, ins.getState().acceleration(), EPS, "Remove calibrated offset"),
                    () -> assertArrayEquals(ZERO, ins.getState().velocity(), EPS, "Stationary phone must not gain speed"),
                    () -> assertArrayEquals(ZERO, ins.getState().position(), EPS, "Stationary phone must not move"));
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.2, -0.2})
        void realAccelerationAboveZuptThresholdIsPreserved(double acceleration) {
            Ins ins = constantAcceleration(acceleration, 0, 0);
            assertArrayEquals(new double[]{acceleration, 0, 0}, ins.getState().velocity(), EPS,
                    "0.2 m/s^2 is motion; squared norm 0.04 must not erase it");
            assertArrayEquals(new double[]{acceleration / 2, 0, 0}, ins.getState().position(), EPS);
        }

        @Test
        void duplicateAccelerationDoesNotReplaceIntegrationHistory() {
            Ins ins = new Ins();
            feed(ins, START, 1, 0, 0);
            feed(ins, START + STEP, 1, 0, 0);
            feed(ins, START + STEP, 50, 0, 0); // Same measurement time, conflicting value.
            feed(ins, START + 2 * STEP, 1, 0, 0);
            assertArrayEquals(new double[]{0.02, 0, 0}, ins.getState().velocity(), EPS,
                    "Ignore duplicate timestamps instead of poisoning the next trapezoid");
            assertArrayEquals(new double[]{0.0002, 0, 0}, ins.getState().position(), EPS);
        }

        @Test
        void coastingAtConstantSpeedDoesNotBecomeAStop() {
            Ins ins = new Ins();
            // Accelerate for 0.2 s, then ramp down to zero over one 0.01 s step.
            for (int i = 0; i <= 20; i++) feed(ins, START + i * STEP, 1, 0, 0);
            feed(ins, START + 21 * STEP, 0, 0, 0);
            assertEquals(0.205, ins.getState().velocity()[0], EPS);
            double coastStartPosition = ins.getState().position()[0];
            for (int i = 22; i <= 71; i++) feed(ins, START + i * STEP, 0, 0, 0);
            assertAll(
                    () -> assertEquals(0.205, ins.getState().velocity()[0], EPS,
                            "Zero acceleration during coasting is not evidence of zero velocity"),
                    () -> assertEquals(coastStartPosition + 0.205 * 0.5,
                            ins.getState().position()[0], EPS));
        }

        @Test
        void rotationVectorCannotBePassedAsAcceleration() {
            Ins ins = new Ins();
            assertThrows(IllegalArgumentException.class,
                    () -> ins.onAcceleration(rotation(START, 0, 0, 0.5), ZERO_CALIBRATION),
                    "Validate the sensor type before integrating dimensionless quaternion components");
        }

        @ParameterizedTest
        @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
        void nonFiniteAccelerationIsRejected(double invalid) {
            Ins ins = new Ins();
            feed(ins, START, 0, 0, 0);
            assertThrows(IllegalArgumentException.class,
                    () -> feed(ins, START + STEP, invalid, 0, 0),
                    "Reject non-finite input before it corrupts velocity and position");
        }

        @Test
        void longNanosecondTimestampDoesNotLosePrecision() {
            Ins ins = new Ins();
            long timestamp = 9_007_199_254_740_993L; // 2^53 + 1, about 104 days of uptime.
            feed(ins, timestamp, 0, 0, 0);
            assertEquals(timestamp, (long) ins.getState().timestamp(),
                    "Store timestamps as long; this nanosecond cannot be represented by double");
        }
    }
}
