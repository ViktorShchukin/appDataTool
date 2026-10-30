package com.anorisno.appDataTool.unit;

import com.anorisno.appDataTool.Zupt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit Jupiter tests for Zupt.java. No Mockito, sleeps, or real clock needed.
 * Place at src/test/java/com/anorisno/appDataTool/ZuptTest.java.
 *
 * Intended contract:
 * - timestamps are strictly increasing, nonnegative nanoseconds;
 * - norm(acc) <= 0.1 is quiet, norm(acc) > 0.1 is motion;
 * - stasis requires STRICTLY MORE than 400 ms since the last observed motion
 *   (or since initialization / a reset if no motion has yet been observed);
 * - a gap STRICTLY GREATER than 100 ms resets the quiet history;
 * - every measurement updates the previous-sample timestamp.
 *
 * Regression tests intentionally fail on the attached implementation. Do not
 * disable them or change their expected results to preserve the timing bugs.
 * This acceleration-only gate cannot prove zero velocity during constant-speed
 * motion; the tests validate its timing, not a complete physical ZUPT detector.
 */
class ZuptTest {
    private static final long MS = 1_000_000L;
    private static final long START = 1_000_000_000L;
    private static final double[] QUIET = {0.0, 0.0, 0.0};
    private static final double[] MOTION = {0.2, 0.0, 0.0};

    @ParameterizedTest
    @MethodSource("firstSamples")
    void firstSampleNeverReportsStasis(double[] acceleration) {
        assertFalse(new Zupt().check(acceleration, START));
    }

    static Stream<Arguments> firstSamples() {
        return Stream.of(Arguments.of((Object) QUIET), Arguments.of((Object) MOTION));
    }

    @ParameterizedTest(name = "motion on {0}")
    @MethodSource("movingSamples")
    void continuousMotionNeverReportsStasis(String description, double[] acceleration) {
        Zupt zupt = new Zupt();
        for (int i = 0; i <= 30; i++) {
            assertFalse(zupt.check(acceleration, START + i * 50 * MS),
                    "Motion must stay false even after 400 ms");
        }
    }

    static Stream<Arguments> movingSamples() {
        return Stream.of(
                Arguments.of("X", new double[]{0.101, 0.0, 0.0}),
                Arguments.of("negative Y", new double[]{0.0, -0.101, 0.0}),
                Arguments.of("Z", new double[]{0.0, 0.0, 0.101}),
                // Neither individual component exceeds 0.1, but the norm does.
                Arguments.of("combined axes", new double[]{0.08, 0.08, 0.0}));
    }

    @ParameterizedTest(name = "quiet acceleration x={0}")
    @ValueSource(doubles = {0.0, 0.099, 0.1, -0.1})
    void valuesAtOrBelowMagnitudeThresholdAllowStasis(double x) {
        Zupt zupt = new Zupt();
        long motionTime = primeWithMotion(zupt);
        double[] quiet = {x, 0.0, 0.0};
        for (int i = 1; i <= 4; i++) {
            assertFalse(zupt.check(quiet, motionTime + i * 100 * MS));
        }
        assertTrue(zupt.check(quiet, motionTime + 400 * MS + 1));
    }

    @Test
    void stasisRequiresStrictlyMoreThan400Milliseconds() {
        Zupt zupt = new Zupt();
        long motionTime = primeWithMotion(zupt);
        for (int i = 1; i <= 3; i++) {
            assertFalse(zupt.check(QUIET, motionTime + i * 100 * MS));
        }
        assertFalse(zupt.check(QUIET, motionTime + 400 * MS - 1));
        assertFalse(zupt.check(QUIET, motionTime + 400 * MS));
        assertTrue(zupt.check(QUIET, motionTime + 400 * MS + 1));
    }

    @Test
    void initiallyQuietPhoneEventuallyReportsStasis() {
        Zupt zupt = new Zupt();
        assertFalse(zupt.check(QUIET, START));
        for (int i = 1; i <= 4; i++) {
            assertFalse(zupt.check(QUIET, START + i * 100 * MS));
        }
        assertTrue(zupt.check(QUIET, START + 400 * MS + 1),
                "Startup at rest must not require a later motion sample to arm the timer");
    }

    @Test
    void initialMotionSampleFollowedByQuietIsNotForgotten() {
        Zupt zupt = new Zupt();
        assertFalse(zupt.check(MOTION, START));
        for (int i = 1; i <= 4; i++) {
            assertFalse(zupt.check(QUIET, START + i * 100 * MS));
        }
        assertTrue(zupt.check(QUIET, START + 400 * MS + 1),
                "The first motion sample must initialize the timer");
    }

    @Test
    void motionImmediatelyClearsStasisAndRestartsTheTimer() {
        Zupt zupt = new Zupt();
        long stationaryTime = establishStasis(zupt);
        long nextMotion = stationaryTime + 50 * MS;
        assertFalse(zupt.check(MOTION, nextMotion));
        for (int i = 1; i <= 4; i++) {
            assertFalse(zupt.check(QUIET, nextMotion + i * 100 * MS));
        }
        assertTrue(zupt.check(QUIET, nextMotion + 400 * MS + 1));
    }

    @Test
    void gapOver100MillisecondsResetsHistoryAndRequiresANewWindow() {
        Zupt zupt = new Zupt();
        long stationaryTime = establishStasis(zupt);
        long afterGap = stationaryTime + 100 * MS + 1;
        assertFalse(zupt.check(QUIET, afterGap),
                "Unobserved time must not count as confirmed quiet time");
        for (int i = 1; i <= 4; i++) {
            assertFalse(zupt.check(QUIET, afterGap + i * 100 * MS));
        }
        assertTrue(zupt.check(QUIET, afterGap + 400 * MS + 1));
    }

    @Test
    void gapOfExactly100MillisecondsDoesNotResetHistory() {
        Zupt zupt = new Zupt();
        long stationaryTime = establishStasis(zupt);
        assertTrue(zupt.check(QUIET, stationaryTime + 100 * MS),
                "The reset comparison is strict: exactly 100 ms is allowed");
    }

    @Test
    void gapIsMeasuredFromPreviousSampleRatherThanFirstSample() {
        Zupt zupt = new Zupt();
        long motionTime = primeWithMotion(zupt);
        // This catches an incomplete fix that reverses the gap subtraction but
        // still forgets to update lastTimestamp on every sample.
        for (int i = 1; i <= 100; i++) {
            boolean actual = zupt.check(QUIET, motionTime + i * 50 * MS);
            if (i <= 8) assertFalse(actual);
            else assertTrue(actual, "Continuous 50 ms samples must remain valid");
        }
    }

    @Test
    void repeatedLongGapsNeverAccumulateStasisTime() {
        Zupt zupt = new Zupt();
        long timestamp = primeWithMotion(zupt);
        for (int i = 0; i < 10; i++) {
            timestamp += 101 * MS;
            assertFalse(zupt.check(QUIET, timestamp),
                    "Every 101 ms gap must restart the quiet timer");
        }
    }

    private static long primeWithMotion(Zupt zupt) {
        // Two samples allow independent tests to get past the startup defect.
        assertFalse(zupt.check(MOTION, START));
        assertFalse(zupt.check(MOTION, START + MS));
        return START + MS;
    }

    private static long establishStasis(Zupt zupt) {
        long motionTime = primeWithMotion(zupt);
        for (int i = 1; i <= 4; i++) {
            assertFalse(zupt.check(QUIET, motionTime + i * 100 * MS));
        }
        long timestamp = motionTime + 400 * MS + 1;
        assertTrue(zupt.check(QUIET, timestamp));
        return timestamp;
    }
}
